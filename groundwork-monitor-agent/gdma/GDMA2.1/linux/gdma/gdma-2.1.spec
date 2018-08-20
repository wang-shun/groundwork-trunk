Name: gdma
Summary: GroundWork Distributed Monitoring Agent
Version: 2.1
Release: 1
License: GroundWork
Group: Applications/Monitoring
Vendor: GroundWork Opensource
Packager: GroundWork Opensource 
ExclusiveOS: linux

%description
GroundWork Distributed Monitoring Agent

%prep
echo "Preparing for GDMA build..."
GDMA_PREFIX=/usr/local/groundwork/gdma
mkdir -p $GDMA_PREFIX
mkdir $GDMA_PREFIX/spool
mkdir $GDMA_PREFIX/log
mkdir $GDMA_PREFIX/tmp
groupadd gdma
useradd -g gdma -d $GDMA_PREFIX gdma

%build
cd gdma/
echo "Building source..."
sh build-gdma.sh
cp -f gdma_install.sh /root/
cp -f gdma_cleanup.sh /root/

%clean
echo "Cleaning up..."
userdel gdma
rm -fr /usr/local/groundwork/gdma
rm -rf /root/gdma_install.sh
rm -rf /root/gdma_cleanup.sh
rm -rf /root/gdma_*.tar.gz

%post
echo "Installing GDMA package..."
cd /root/
sh gdma_install.sh

%preun
echo "Cleaning up GDMA..."
service gdma stop
cd /root/
sh gdma_cleanup.sh

%files
/root/gdma_install.sh
/root/gdma_cleanup.sh
/root/gdma_*.tar.gz
