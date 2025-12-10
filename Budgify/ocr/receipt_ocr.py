from flask import Flask, request, jsonify
import requests
import time # Time is kept but is less necessary for OCR.space, but can be used for logging/debugging
import re
from datetime import datetime, date
import logging # For logging within Flask app

app = Flask(__name__)

# --- Configuration for OCR.space ---
# You need to replace "YOUR_OCR_SPACE_API_KEY" with your actual key.
# The endpoint is for the free/pro Plan. Use the appropriate one.
OCR_SPACE_API_KEY = "K82628630788957"
OCR_SPACE_ENDPOINT = "https://api.ocr.space/parse/image"

# Configure logging (optional, but good practice for debugging)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# --- OCR.space Scan Route ---
@app.route('/scan_receipt', methods=['POST'])
def scan_receipt_ocrspace():
    if 'receipt_image' not in request.files:
        logger.error("No image file provided in the request.")
        return jsonify({'error': 'No image file provided'}), 400

    image_file = request.files['receipt_image']

    payload = {
        'apikey': OCR_SPACE_API_KEY,
        'language': 'eng',
        'isOverlayRequired': True,
        'isTable': True,
        'OCREngine': 2,
    }

    files = {
        'file': (image_file.filename, image_file.stream, image_file.mimetype)
    }

    try:
        ocr_response_raw = requests.post(
            OCR_SPACE_ENDPOINT,
            data=payload,
            files=files
        )
        ocr_response_raw.raise_for_status()

        ocr_result_data = ocr_response_raw.json()

        if ocr_result_data.get('IsErroredOnProcessing'):
            error_message = ocr_result_data.get('ErrorMessage', ['Unknown OCR Error'])
            logger.error(f"OCR Processing Failed: {error_message}")
            # Ensure consistent error response format
            return jsonify({
                'amount': 0.0,
                'description': 'OCR Processing Failed',
                'date': date.today().strftime("%d/%m/%Y"),
                'category': 'Error',
                'details': error_message # Include details for client debug
            }), 500

        parsed_results = ocr_result_data.get('ParsedResults')
        if not parsed_results or not parsed_results[0].get('ParsedText'):
            logger.warning("No parsed text found in OCR response.")
            # Ensure consistent error response format
            return jsonify({
                'amount': 0.0,
                'description': 'Failed to extract text',
                'date': date.today().strftime("%d/%m/%Y"),
                'category': 'Error',
                'details': ['OCR returned no readable text.']
            }), 500

        raw_text = parsed_results[0]['ParsedText']
        logger.info(f"Raw OCR Text:\n{raw_text}")

        # --- IMPORTANT: Call server-side parsing function ---
        parsed_transaction = parse_ocr_text_server(raw_text)

        logger.info(f"Successfully parsed transaction: {parsed_transaction}")

        # --- CRITICAL CHANGE: Return parsed_transaction dictionary directly ---
        return jsonify(parsed_transaction), 200

    except requests.exceptions.RequestException as e:
        logger.error(f"OCR.space API Request Error: {e}")
        # Ensure consistent error response format
        return jsonify({
            'amount': 0.0,
            'description': 'Network Error',
            'date': date.today().strftime("%d/%m/%Y"),
            'category': 'Error',
            'details': [str(e)]
        }), 500
    except Exception as e:
        logger.error(f"Server-side parsing error: {e}", exc_info=True)
        # Ensure consistent error response format
        return jsonify({
            'amount': 0.0,
            'description': 'Internal Server Error',
            'date': date.today().strftime("%d/%m/%Y"),
            'category': 'Error',
            'details': [str(e)]
        }), 500

# --- Server-side parsing function (improved for robust amount parsing) ---
def parse_ocr_text_server(text: str) -> dict:
    lines = text.split('\n')
    highest_amount = 0.0
    description = "Scanned Receipt"
    transaction_date = date.today()  # Default to today

    # 1. Improved Amount Parsing
    # Regex to find monetary values, ensuring a decimal part of exactly two digits
    amount_patterns = [
        # Prioritize patterns preceded by common "total" keywords
        re.compile(r'(?:TOTAL|AMOUNT|BALANCE|SUM|DUE|TOTALE|TOTALE COMPLESSIVO|DOVUTO|IMPORTO|IMPORTO DOVUTO)\s*[:\-\s]*\$?€?\s*(\d{1,3}(?:[.,]\d{3})*[.,]\d{2})\b', re.IGNORECASE),
        # General pattern for numbers that look like currency (e.g., 54.50, 1.234,50, 1,234.50)
        # Ensure it has a fractional part of exactly two digits
        re.compile(r'\b\$?€?\s*(\d{1,3}(?:[.,]\d{3})*[.,]\d{2})\b')
    ]

    for line in lines:
        for pattern in amount_patterns:
            for match in pattern.finditer(line):
                try:
                    amount_str = match.group(1)

                    # Heuristic to determine decimal separator based on the last separator before 2 digits
                    last_decimal_match_comma = re.search(r',(\d{2})$', amount_str)
                    last_decimal_match_dot = re.search(r'\.(\d{2})$', amount_str)

                    if last_decimal_match_comma:
                        # Assume European style (comma is decimal, dot is thousand)
                        amount_str = amount_str.replace('.', '')  # Remove thousand separators
                        amount_str = amount_str.replace(',', '.')  # Replace decimal comma with dot
                    elif last_decimal_match_dot:
                        # Assume US style (dot is decimal, comma is thousand)
                        amount_str = amount_str.replace(',', '')  # Remove thousand separators
                        # No need to replace dot, as it's already a decimal dot
                    # If neither, it means it's likely already a simple decimal or integer-like, so no replacement on separators

                    current_amount = float(amount_str)
                    highest_amount = max(highest_amount, current_amount)
                except ValueError:
                    logger.debug(f"Failed to convert amount string '{amount_str}' to float during server-side parsing.")
                    continue

    # 2. Improved Date Parsing
    # Ordered list of regex patterns and corresponding format strings for datetime.strptime
    date_formats = [
        # YYYY-MM-DD
        ("%Y-%m-%d", re.compile(r'\b\d{4}-\d{2}-\d{2}\b')),
        # DD/MM/YYYY
        ("%d/%m/%Y", re.compile(r'\b\d{1,2}/\d{1,2}/\d{4}\b')),
        # DD-MM-YYYY
        ("%d-%m-%Y", re.compile(r'\b\d{1,2}-\d{1,2}-\d{4}\b')),
        # DD.MM.YYYY
        ("%d.%m.%Y", re.compile(r'\b\d{1,2}\.\d{1,2}\.\d{4}\b')),
        # MM/DD/YYYY (placed after DD/MM/YYYY to prefer European style if ambiguous for 4-digit year)
        ("%m/%d/%Y", re.compile(r'\b\d{1,2}/\d{1,2}/\d{4}\b')),
        # MM-DD-YYYY
        ("%m-%d-%Y", re.compile(r'\b\d{1,2}-\d{1,2}-\d{4}\b')),

        # Month Name (MMM or MMMM) DD, YYYY (e.g., Jan 01, 2025 or January 01, 2025)
        # re.IGNORECASE is crucial here for varying capitalization
        ("%b %d, %Y", re.compile(r'\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2},\s+\d{4}\b', re.IGNORECASE)),
        ("%B %d, %Y", re.compile(r'\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\s+\d{1,2},\s+\d{4}\b', re.IGNORECASE)),
        # DD Month Name YYYY (e.g., 01 Jan 2025)
        ("%d %b %Y", re.compile(r'\b\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{4}\b', re.IGNORECASE)),
        ("%d %B %Y", re.compile(r'\b\d{1,2}\s+(?:January|February|March|April|May|June|July|August|September|October|November|December)\s+\d{4}\b', re.IGNORECASE)),
        # YY formats (less reliable, put last)
        ("%d/%m/%y", re.compile(r'\b\d{1,2}/\d{1,2}/\d{2}\b')),
        ("%d-%m-%y", re.compile(r'\b\d{1,2}-\d{1,2}-\d{2}\b')),
        ("%m/%d/%y", re.compile(r'\b\d{1,2}/\d{1,2}/\d{2}\b')),
        ("%m-%d-%y", re.compile(r'\b\d{1,2}-\d{1,2}-\d{2}\b'))
    ]

    found_date = None
    for line in lines:
        for fmt, pattern in date_formats:
            match = pattern.search(line)
            if match:
                date_string = match.group(0)
                try:
                    # Python's strptime handles month names and locales automatically
                    parsed_date = datetime.strptime(date_string, fmt).date()
                    found_date = parsed_date
                    logger.info(f"Server: Successfully parsed date '{date_string}' with format '{fmt}' to {found_date}")
                    break # Stop looking for dates in this line if one is found
                except ValueError:
                    logger.debug(f"Server: Failed to parse date '{date_string}' with format '{fmt}' in line: '{line.strip()}'")
        if found_date:
            break # Stop looking for dates in other lines if one is found

    if not found_date:
        logger.info("Server: No date found in text after trying all patterns. Defaulting to today.")
        transaction_date = date.today()
    else:
        transaction_date = found_date

    # --- 3. Improved Description (Store Name) Parsing ---
    potential_descriptions = []

    # Common keywords often found in receipt headers/footers, not actual store names
    exclusion_keywords = [
        re.compile(r'TAX(?:ABLE)?', re.IGNORECASE),
        re.compile(r'VAT', re.IGNORECASE),
        re.compile(r'TOTAL', re.IGNORECASE),
        re.compile(r'AMOUNT', re.IGNORECASE),
        re.compile(r'BALANCE DUE', re.IGNORECASE),
        re.compile(r'CHANGE', re.IGNORECASE),
        re.compile(r'CASH', re.IGNORECASE),
        re.compile(r'CREDIT CARD', re.IGNORECASE),
        re.compile(r'DEBIT CARD', re.IGNORECASE),
        re.compile(r'THANK YOU', re.IGNORECASE),
        re.compile(r'CUSTOMER COPY', re.IGNORECASE),
        re.compile(r'TRANSACTION(?: ID)?', re.IGNORECASE),
        re.compile(r'ORDER(?: #)?', re.IGNORECASE),
        re.compile(r'SUBTOTAL', re.IGNORECASE),
        re.compile(r'VISA', re.IGNORECASE),
        re.compile(r'MASTERCARD', re.IGNORECASE),
        re.compile(r'TEL:', re.IGNORECASE),
        re.compile(r'PHONE:', re.IGNORECASE),
        re.compile(r'WWW\.', re.IGNORECASE),
        re.compile(r'\.COM', re.IGNORECASE),
        re.compile(r'GST', re.IGNORECASE),
        re.compile(r'HST', re.IGNORECASE),
        re.compile(r'^\d{4,}$'), # Lines that are just long numbers (e.g. barcodes, transaction IDs)
        re.compile(r'^\s*[\d\s]*\d{1,3}(?:[.,]\d{3})*[.,]\d{2}\s*$') # Lines that are just amounts
    ]

    # Iterate through the first few lines, as store names are usually at the top
    for i, line in enumerate(lines):
        stripped_line = line.strip()
        if not stripped_line:
            continue

        # Exclude lines that match known "noise" patterns
        is_noise = False
        for keyword_pattern in exclusion_keywords:
            if keyword_pattern.search(stripped_line):
                is_noise = True
                logger.debug(f"Server: Excluding line '{stripped_line}' for description due to keyword match: '{keyword_pattern.pattern}'")
                break

        if is_noise:
            continue

        # Heuristic: Prioritize lines that look like names (e.g., predominantly alphabetic, capitalized words)
        # We look for lines with at least two words, or a single prominent capitalized word.
        words = stripped_line.split()
        is_potential_store_name = False

        # Check for multiple capitalized words or a strong capitalized single word
        if len(words) > 1 and all(word[0].isupper() for word in words if word and word[0].isalpha()):
            is_potential_store_name = True # E.g., "The Coffee Shop"
        elif len(words) == 1 and words[0].isupper() and len(words[0]) > 2:
            is_potential_store_name = True # E.g., "WALMART" (all caps)
        # Also consider lines with a good mix of alpha characters, not just numbers
        elif re.search(r'[A-Za-z]{3,}', stripped_line) and not re.search(r'\d', stripped_line): # At least 3 consecutive letters, and no digits
             is_potential_store_name = True

        # Avoid lines that are too short to be meaningful or excessively long (might be garbage OCR)
        # Cap the number of lines to check for efficiency
        if (len(stripped_line) > 3 and len(stripped_line) < 50 and is_potential_store_name) or i < 3: # Always consider first 3 lines if not noise
            potential_descriptions.append((i, stripped_line)) # Store line index and text
            logger.debug(f"Server: Identified potential store name candidate: '{stripped_line}'")

        if i >= 6 and potential_descriptions: # Only consider the first few lines for main store name
            break

    # Select the best description: prefer earlier and more prominent-looking ones
    if potential_descriptions:
        # Sort by: 1. Appears in the first few lines (lower index), 2. Length (longer is better), 3. Starts with uppercase
        potential_descriptions.sort(key=lambda x: (x[0], -len(x[1]), not x[1][0].isupper() if x[1] else True))
        description = potential_descriptions[0][1]
        logger.info(f"Server: Selected store name description: '{description}' (from line {potential_descriptions[0][0]})")
    else:
        # Fallback to the first non-blank line if no good store name candidate found
        fallback_description_text = ""
        for line in lines:
            stripped_line = line.strip()
            if stripped_line:
                fallback_description_text = stripped_line
                break
        if fallback_description_text:
            description = fallback_description_text
            logger.info(f"Server: Falling back to first non-blank line for description: '{description}'")
        else:
            description = "Scanned Receipt"
            logger.info("Server: No suitable description found, defaulting to 'Scanned Receipt'.")

    # Ensure description isn't too long for the UI field
    if len(description) > 30:
        # Truncate and add ellipsis if it was originally longer
        description = description[:27].strip() + "..." if len(description) > 27 and len(description) > 30 else description[:30].strip()
        logger.info(f"Server: Truncated description to: '{description}'")

    return {
        "amount": highest_amount,
        "description": description,
        "date": transaction_date.strftime("%d/%m/%Y"),  # Format as "dd/MM/yyyy"
        "category": "General"  # Default category, can be improved later
    }
