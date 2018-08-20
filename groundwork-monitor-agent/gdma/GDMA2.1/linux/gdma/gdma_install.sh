#!/bin/sh

GDMA_INST_DIR=/usr/local/groundwork
mkdir -p $GDMA_INST_DIR
groupadd gdma
useradd -g gdma -d $GDMA_INST_DIR/gdma gdma

tar -xzf ./gdma_*.tar.gz -C /

cd $GDMA_INST_DIR
chown -R gdma.gdma gdma
cp gdma/etc.init.d/gdma /etc/init.d
chmod +x /etc/init.d/gdma
chkconfig --add gdma
chkconfig gdma on
chmod +x $GDMA_INST_DIR/gdma/bin/send_nsca.pl

service gdma start
