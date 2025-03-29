import json
import os
from flask import Flask, jsonify, send_from_directory
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
import time
from selenium.common.exceptions import TimeoutException

app = Flask(__name__, static_folder='static')

print(f"Static folder path: {os.path.abspath(app.static_folder)}")

class CVData:
    def __init__(self):
        self.name = ""
        self.skills = []
        self.education = []
        self.experience = ""
        self.location = ""
        self.languages = []
        self.job_titles = []
        self.certifications = []

def load_cv_data(json_file="cv_data.json"):
    cv_data = CVData()
    try:
        with open(json_file, "r") as f:
            data = json.load(f)
            cv_data.name = data.get("name", "")
            cv_data.skills = data.get("skills", "").split(",") if data.get("skills") else []
            cv_data.education = data.get("education", "").split(",") if data.get("education") else []
            cv_data.experience = data.get("experience", "")
            cv_data.location = data.get("location", "")
            cv_data.languages = data.get("languages", "").split(",") if data.get("languages") else []
            cv_data.job_titles = data.get("jobTitles", "").split(",") if data.get("jobTitles") else []
            cv_data.certifications = data.get("certifications", "").split(",") if data.get("certifications") else []
            
            print("CV Data Loaded:")
            print(f"Name: {cv_data.name}")
            print(f"Skills: {cv_data.skills}")
            print(f"Education: {cv_data.education}")
            print(f"Experience: {cv_data.experience}")
            print(f"Location: {cv_data.location}")
            print(f"Languages: {cv_data.languages}")
            print(f"Job Titles: {cv_data.job_titles}")
            print(f"Certifications: {cv_data.certifications}")
            print("-" * 50)
            
        return cv_data
    except FileNotFoundError:
        print("Error: cv_data.json not found")
        return None
    except json.JSONDecodeError:
        print("Error: cv_data.json is invalid JSON")
        return None
    except Exception as e:
        print(f"Error loading CV data: {e}")
        return None

def search_linkedin_jobs(cv_data):
    if not cv_data:
        print("No CV data provided for search")
        return []

    skills = " ".join(cv_data.skills[:5])
    search_query = f'"Internship" {skills}' if skills else '"Internship" Java Python'
    location = cv_data.location.strip() if cv_data.location and cv_data.location.strip() else "Bucharest, Romania"
    
    print("Search Parameters:")
    print(f"Search Query: {search_query}")
    print(f"Location: {location}")
    print(f"Constructed URL: https://www.linkedin.com/jobs/search/?keywords={search_query.replace(' ', '%20')}&location={location.replace(' ', '%20')}")
    print("-" * 50)

    url = f"https://www.linkedin.com/jobs/search/?keywords={search_query.replace(' ', '%20')}&location={location.replace(' ', '%20')}"

    options = Options()
    options.add_argument("--headless=new")
    options.add_argument("--disable-gpu")
    options.add_argument("--log-level=3")
    options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
    driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)

    driver.get(url)
    time.sleep(3)

    last_height = driver.execute_script("return document.body.scrollHeight")
    for _ in range(3):
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(2)
        new_height = driver.execute_script("return document.body.scrollHeight")
        if new_height == last_height:
            break
        last_height = new_height

    jobs = []
    try:
        WebDriverWait(driver, 20).until(EC.presence_of_element_located((By.CSS_SELECTOR, ".job-search-card")))
        job_cards = driver.find_elements(By.CSS_SELECTOR, ".job-search-card")
        for job in job_cards:
            title = job.find_element(By.CSS_SELECTOR, ".base-search-card__title").text.strip()
            link = job.find_element(By.CSS_SELECTOR, "a.base-card__full-link").get_attribute("href")
            company_elements = job.find_elements(By.CSS_SELECTOR, ".base-search-card__subtitle")
            company = company_elements[0].text.strip() if company_elements else "Unknown Company"
            location_elements = job.find_elements(By.CSS_SELECTOR, ".job-search-card__location")
            job_location = location_elements[0].text.strip() if location_elements else "Not specified"
            time_elements = job.find_elements(By.CSS_SELECTOR, "time.job-search-card__listdate")
            posted = time_elements[0].get_attribute("datetime") if time_elements else "Not specified"
            jobs.append({
                "title": title,
                "company": company,
                "location": job_location,
                "posted": posted,
                "link": link
            })
    except TimeoutException:
        print("Timeout waiting for job cards")
    finally:
        driver.quit()

    print("Job Search Results:")
    if jobs:
        for i, job in enumerate(jobs, 1):
            print(f"Job {i}:")
            print(f"  Title: {job['title']}")
            print(f"  Company: {job['company']}")
            print(f"  Location: {job['location']}")
            print(f"  Posted: {job['posted']}")
            print(f"  Link: {job['link']}")
            print("-" * 30)
    else:
        print("No jobs found")
    print("=" * 50)

    return jobs

@app.route('/api/jobs')
def get_linkedin_jobs():
    cv_data = load_cv_data()
    if not cv_data:
        return jsonify({"error": "No CV data available"}), 400
    jobs = search_linkedin_jobs(cv_data)
    return jsonify({"jobs": jobs})

@app.route('/')
def serve_frontend():
    return send_from_directory(app.static_folder, 'index.html')

if __name__ == "__main__":
    app.run(port=5000)