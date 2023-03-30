FROM python:3.10.4-slim

COPY . /Flask
WORKDIR /Flask
RUN apt-get update -y --fix-missing
RUN apt install libgl1-mesa-glx -y
RUN apt-get install 'ffmpeg'\
    'libsm6'\
    'libxext6'  -y
RUN pip3 install --upgrade pip
RUN pip install -r requirements.txt
EXPOSE 8080
ENTRYPOINT ["gunicorn", "--bind=0.0.0.0:8080", "wsgi:app"]