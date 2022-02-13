FROM tomcat:9.0.16-jre8

# ENV CATALINA_OPTS="-XX:PermSize=256m -XX:MaxPermSize=768m -server -Xms256m -Xmx1g -XX:SurvivorRatio=6 -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=68 -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark -XX:+HeapDumpOnOutOfMemoryError"

ADD target/ideas-dockercompose-language*.war ./webapps/ideas-dockercompose-language.war


CMD  ./bin/catalina.sh run