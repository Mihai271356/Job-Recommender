# Job Recommender 📋
A job-matching app using Python, Flask, and Selenium to scrape LinkedIn, with Java, Apache PDFBox for CV parsing, and an HTML/CSS/JavaScript frontend with a RESTful API.

## What It Does 🔍
- Reads your CV data.
- Searches LinkedIn for internships using your top skills and location.
- Shows a list of jobs with titles, dates, and links.

## Tech used 🛠️
- Python, Flask, Selenium.
- Java and Apache PDFBox for CV parsing.
- HTML, CSS, JavaScript for the webpage.

## How to Set It Up ⚙️
```
git clone https://github.com/Mihai271356/Job-Recommender.git
cd Job-Recommender
python -m venv venv
.\venv\Scripts\activate
pip install -r requirements.txt
python scrapeLinkedin.py
```
Open your browser and go to:
http://127.0.0.1:5000
