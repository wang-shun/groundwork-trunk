for f in `curl -s 'http://localhost:4242/api/suggest?type=metrics&max=20' |python -mjson.tool | sed -e 's/\[//g; s/\]//g;s/.* \"//g;s/\"//g;s/,//g'`
do
curl “http://localhost:4242//api/query?start=1y-ago&m=sum:$f” | pyhton -mjson.tool > /tmp/$f.txt
done