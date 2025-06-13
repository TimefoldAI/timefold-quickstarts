import logging.config
import os
import uvicorn

def main():
    logging_conf_path = os.path.join(os.path.dirname(__file__), '..', '..', 'logging.conf')
    if os.path.exists(logging_conf_path):
        logging.config.fileConfig(logging_conf_path)
    
    uvicorn.run("meeting_scheduling.rest_api:app", host="0.0.0.0", port=8000, reload=True)

if __name__ == "__main__":
    main()