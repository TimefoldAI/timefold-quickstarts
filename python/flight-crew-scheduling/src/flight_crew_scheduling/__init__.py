import uvicorn

from .rest_api import app


def main():
    config = uvicorn.Config("flight_crew_scheduling:app",
                            port=8080,
                            log_config="logging.conf",
                            use_colors=True)
    server = uvicorn.Server(config)
    server.run()


if __name__ == "__main__":
    main()
