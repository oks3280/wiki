from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
from lxml import html
import datetime

# Create a new Chrome driver instance
options = Options()
options.executable_path = ChromeDriverManager().install()
options.add_argument('--headless')
driver = webdriver.Chrome(options=options)

# Function to collect data from the current page
def collect_data(url):
    # Load the URL
    driver.get(url)

    # Parse the current page using lxml
    tree = html.fromstring(driver.page_source)

    # Use XPath to locate the table
    table = tree.xpath('/html/body/div[3]/div[2]/div[2]/div[3]/table[1]/tbody')[0]

    # Create an empty list to store the data rows
    data_rows = []

    # Iterate through rows and extract data
    rows = table.xpath('.//tr')
    for row in rows:
        # Extract and store the text content of each cell in the row
        cells = row.xpath('.//td')
        row_data = []
        for cell in cells:
            cell_text = cell.text_content().strip()
            if cell_text:
                row_data.append(cell_text)
        if row_data:
            data_rows.append(row_data)

    return data_rows

# Define the base URL without the page number
base_url = "https://finance.naver.com/sise/sise_market_sum.naver?sosok=0&page={}"

# Start page number
page_number = 1

# Get the current date and time
current_datetime = datetime.datetime.now()

# Extract the year, month, day, hour, minute, and second from the current date and time
current_year = current_datetime.strftime("%Y")
current_time = current_datetime.strftime("%H%M%S")

# Create a filename with the current year, month, day, and time
filename = f"./logs/output_{current_year}_{current_time}.txt"

# Open the file for writing
with open(filename, "w", encoding="utf-8") as file:
    # Loop through pages and collect data
    while True:
        # Construct the URL for the current page
        current_url = base_url.format(page_number)

        # Collect data from the current page
        data_rows = collect_data(current_url)

        # If there is no data, exit the loop
        if not data_rows:
            print("더 이상 페이지 없음 - 종료")
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
