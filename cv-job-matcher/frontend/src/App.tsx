import React, { useEffect, useState } from "react";
import axios from "axios";

interface CVData {
  name: string;
  skills: string;
  education: string;
  experience: string;
  location: string;
  languages: string;
  jobTitles: string;
  certifications: string;
}

interface Job {
  title: string;
  company: string;
  location: string;
  posted: string;
  link: string;
}

const App: React.FC = () => {
  const [cvData, setCVData] = useState<CVData | null>(null);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const cvResponse = await axios.get("http://localhost:8080/api/cv");
        setCVData(cvResponse.data);
        const jobsResponse = await axios.get("http://localhost:5000/api/jobs");
        setJobs(jobsResponse.data.jobs || []);
      } catch (error) {
        console.error("Error fetching data:", error);
      }
      setLoading(false);
    };
    fetchData();
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <h1 className="text-4xl font-bold text-center mb-8">Job Matcher</h1>
      {loading ? (
        <p className="text-center">Loading...</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-2xl font-semibold mb-4">Your Profile</h2>
            {cvData ? (
              <>
                <p><strong>Name:</strong> {cvData.name}</p>
                <p><strong>Skills:</strong> {cvData.skills}</p>
                <p><strong>Education:</strong> {cvData.education}</p>
                <p><strong>Experience:</strong> {cvData.experience}</p>
                <p><strong>Location:</strong> {cvData.location}</p>
                <p><strong>Languages:</strong> {cvData.languages}</p>
                <p><strong>Job Titles:</strong> {cvData.jobTitles}</p>
                <p><strong>Certifications:</strong> {cvData.certifications}</p>
              </>
            ) : (
              <p>No CV data available.</p>
            )}
          </div>
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-2xl font-semibold mb-4">Matching Jobs</h2>
            {jobs.length > 0 ? (
              <ul className="space-y-4">
                {jobs.map((job, index) => (
                  <li key={index} className="border-b pb-2">
                    <h3 className="text-lg font-medium">{job.title}</h3>
                    <p><strong>Company:</strong> {job.company}</p>
                    <p><strong>Location:</strong> {job.location}</p>
                    <p><strong>Posted:</strong> {job.posted}</p>
                    <a href={job.link} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">
                      Apply Now
                    </a>
                  </li>
                ))}
              </ul>
            ) : (
              <p>No jobs found.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default App;