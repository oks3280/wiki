import requests
from bs4 import BeautifulSoup
from lxml import html
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
import cx_Oracle


# Create an Options object
options = Options()

# Set the executable path of the Chrome driver
options.executable_path = ChromeDriverManager().install()
options.add_argument('--headless')

# Create a new Chrome driver instance
driver = webdriver.Chrome(options=options)
# Get the Google website
driver.get("https://finance.naver.com/sise/sise_market_sum.naver?sosok=0")

# Get the HTML source code of the current web page
html_source = driver.page_source

# Quit the Chrome driver
driver.quit()

# Parse the HTML source code using the BeautifulSoup library
soup = BeautifulSoup(html_source, 'html.parser')

tree = html.fromstring(str(soup))

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

# Print the data rows as arrays
for data_row in data_rows:
    print(data_row)
