from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
from lxml import html
import datetime
import configparser
import os

# Load configuration from the properties file
config = configparser.ConfigParser()
config.read('./config/config.properties')

# Create a new Chrome driver instance
options = Options()
options.executable_path = ChromeDriverManager().install()
options.add_argument('--headless')
driver = webdriver.Chrome(options=options)

# Function to collect data from the current page
def collect_data(url, table_xpath, row_xpath, cell_xpath):
    # Load the URL
    driver.get(url)

    # Parse the current page using lxml
    tree = html.fromstring(driver.page_source)

    # Use XPath to locate the table
    table = tree.xpath(table_xpath)[0]

    # Create an empty list to store the data rows
    data_rows = []

    # Iterate through rows and extract data
    rows = table.xpath(row_xpath)
    for row in rows:
        # Extract and store the text content of each cell in the row
        cells = row.xpath(cell_xpath)
        row_data = []
        for cell in cells:
            cell_text = cell.text_content().strip()
            if cell_text:
                row_data.append(cell_text)
        if row_data:
            data_rows.append(row_data)

    return data_rows

# Get the current date and time
current_datetime = datetime.datetime.now()

# Iterate through groups defined in the configuration
for group in config.sections():
    if group == 'DEFAULT':
        continue

    # Extract configurations for the current group
    base_url = config.get(group, 'base_url')
    table_xpath = config.get(group, 'table_xpath')
    row_xpath = config.get(group, 'row_xpath')
    cell_xpath = config.get(group, 'cell_xpath')
    log_file_creation = config.get(group, 'log_file_creation')
    log_file_directory = config.get(group, 'log_file_directory')

    # Extract the year, month, day, hour, minute, and second from the current date and time
    current_year = current_datetime.strftime("%Y")
    current_time = current_datetime.strftime("%H%M%S")

    # Create a filename with the current year, month, day, and time
    filename = f"output_{current_year}_{current_time}.txt"

    # Open the file for writing if log_file_creation is 'Y'
    if log_file_creation == 'Y':
        log_directory = os.path.join(log_file_directory, group)
        os.makedirs(log_directory, exist_ok=True)
        log_file_path = os.path.join(log_directory, filename)
        with open(log_file_path, "w", encoding="utf-8") as file:
            # Loop through pages and collect data
            page_number = 1
            while True:
                # Construct the URL for the current page
                current_url = base_url.format(page_number)

                # Collect data from the current page
                data_rows = collect_data(current_url, table_xpath, row_xpath, cell_xpath)

                # If there is no data, exit the loop
                if not data_rows:
                    print(f"더 이상 페이지 없음 ({group}) - 종료")
                    break

                # Print the data
                for data_row in data_rows:
                    print(data_row)

                # Write the data to the file
                for data_row in data_rows:
                    file.write("\t".join(data_row) + "\n")

                # Increment the page number for the next iteration
                page_number += 1

# Quit the Chrome driver
driver.quit()
