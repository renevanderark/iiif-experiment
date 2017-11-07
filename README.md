# IIF experiment using OpenJPEG decoder

This proof of concept provides a Dropwizard (java) IIF (level 0) compliant webserver, employing the 2.3 release of OpenJPEG through a JNI binding.

## Quick start using docker

From a dedicated working directory the following instructions should pull the latest docker image from docker hub and start the service.
```bash
mkdir cache # a local directory for image cache
mkdir conf # a local directory for service configuration
wget https://raw.githubusercontent.com/renevanderark/iiif-experiment/master/conf/config.yaml -O conf/config.yaml # donwload the sample configuration
docker run -p9014:8080  -v `pwd`/conf:/conf -v `pwd`/cache:/cache -t renevanderark/iiif-experiment # pull and start the docker container
``` 

Visit the following link to check the installation:
http://localhost:9014/iiif-service/ddd:010691737:mpeg21:p003:image/square/pct:10/0/default.jpg

## Notes

This is a proof of concept project, however, feedback is welcome. For a reference to installation of this service on your local system, the Dockerfile provides a good starting point.

To perform comparisons between OpenJPEG 2.3 and 2.0 another docker image named ```renevanderark/iiif-experiment-opj2``` is also available on docker hub.

To try this version replace the flag ```-t renevanderark/iiif-experiment``` with ```-t renevanderark/iiif-experiment-opj2``` in above instruction.
