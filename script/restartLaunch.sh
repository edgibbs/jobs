#!/bin/bash

#
# /opt/rundeck/jobs/Devops/restartLaunch.sh 
#

export RD_PROJECT=Indexer_Jobs
export RD_URL=https://rundeck.prod-dc.cwds.io:4443
export RD_INSECURE_SSL=true
export RD_USER=admin

PeopleTimestamp=`cat /opt/rundeck/jobs/jobrunner/people_summary.time`
PeopleTimestamp1=`date "+%s" -d "${PeopleTimestamp}"`
CurrentTime=`date "+%s"`
TimeDiff=`expr $CurrentTime - $PeopleTimestamp1`

echo "Current  Time: $CurrentTime"
echo "Timestamp File: $PeopleTimestamp1"

TimeDiffMin=`expr ${TimeDiff} / 60`

if [ ${TimeDiffMin} -gt 10 ]
then
	echo "Stopping Rundeck LaunchCommand"
	rd run --job Kill_LaunchCommand
	sleep 10
	echo "Starting the LaunchCommand"
	rd run --job LaunchCommand

else
   echo " We are good not restarting LaunchCommand"
fi

