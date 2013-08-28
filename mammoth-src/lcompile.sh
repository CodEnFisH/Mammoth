compile=$1

if [ $compile = "yes" ]; then
        ant
        ant examples
fi

cd /home/s3gator/hadoop-0.20.203.0/build/examples
jar cf hadoop-examples.jar org/
mv /home/s3gator/hadoop-0.20.203.0/build/examples/hadoop-examples.jar /home/s3gator/hadoop-0.20.203.0/build

