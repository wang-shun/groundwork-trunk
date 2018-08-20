#!/bin/sh  
# create tarball for use with "gdma_install.sh" or similar script
# deficiencies:  
# needs to be architecture independent
# 5 19 2009 GroundWork

if [ -f /etc/redhat-release ]; then
  osname=`cat /etc/redhat-release|sed -e 's/.*\(ES\|AS\|Enterprise Linux Server\) release.*/rhel/'`
  if ! [ "$osname" == "rhel" ]; then
    osname=''
  fi
  osmaj=`cat /etc/redhat-release|sed -e 's/.*release \([0-9]\+\).*/\1/'`
  osarch=`uname -p`
elif [ -f /etc/SuSE-release ]; then
  osname=`cat /etc/SuSE-release | head -1 |sed -e 's/.*\(Enterprise Server\).*/suse/'`
  if ! [ "$osname" == "suse" ]; then
    osname=''
  fi
  osmaj=`cat /etc/SuSE-release| head -1 |sed -e 's/.* \([0-9]\+\) .*/\1/'`
  osarch=`uname -p`
fi

if ! [ $osname ]; then
  echo "Unsupported operating system.  Cannot continue."
  exit 1
else
  echo "Building for operating system: $osname$osmaj.$osarch"
fi

GDMA_PREFIX=/usr/local/groundwork/gdma
if [ "$osarch" == "x86_64" ]; then
  PERL5LIB=$GDMA_PREFIX/lib64/perl5/site_perl
else
  PERL5LIB=$GDMA_PREFIX/lib/perl5/site_perl
fi

if [ "$osarch" == "x86_64" ]; then
  GDMALIB=$GDMA_PREFIX/lib64
else
  GDMALIB=$GDMA_PREFIX/lib
fi

echo $PWD
cd gdma-core ; tar -cf - . | ( cd /usr/local/groundwork/gdma  ; tar -xpf - )
cd ..

tar -xzf Compress-Raw-Zlib-2.015.tar.gz
tar -xzf Compress-Zlib-2.015.tar.gz
tar -xzf Crypt-SSLeay-0.57.tar.gz
tar -xzf HTML-Parser-3.58.tar.gz
tar -xzf HTML-Tagset-3.20.tar.gz
tar -xzf IO-Compress-Base-2.015.tar.gz
tar -xzf IO-Compress-Zlib-2.015.tar.gz
tar -xzf libwww-perl-5.820.tar.gz
tar -xzf openssl-0.9.7l.tar.gz
tar -xzf MIME-Base64-3.07.tar.gz
tar -xzf URI-1.37.tar.gz
tar -xzf Net-Telnet-3.03.tar.gz
tar -xzvf Storable-2.20.tar.gz

# need to patch the configure file change in openssl!

cd MIME-Base64-3.07
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../URI-1.37
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../IO-Compress-Base-2.015
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../Compress-Raw-Zlib-2.015/
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../IO-Compress-Zlib-2.015/
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../Compress-Zlib-2.015/
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../IO-Compress-Zlib-2.015/
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../HTML-Tagset-3.20/
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../HTML-Parser-3.58/
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../openssl-0.9.7l/
make clean
./config --prefix=/usr/local/groundwork/gdma --openssldir=/usr/local/groundwork/gdma/openssl --shared
make
make install
cd ../Crypt-SSLeay-0.57
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB --lib=$GDMA_PREFIX/lib --shared
LD_RUN_PATH="$GDMA_PREFIX/lib" make
make install
cd ../libwww-perl-5.820
make clean
perl Makefile.PL PREFIX=$GDMA_PREFIX LIB=$PERL5LIB
make
make install
cd ../Net-Telnet-3.03
perl Makefile.PL INSTALLSITELIB=$PERL5LIB INSTALLMAN3DIR=$GDMA_PREFIX/share/man/man3/
make
make test
make pure_install
cd ../Storable-2.20
perl Makefile.PL LIB=$PERL5LIB INSTALLMAN1DIR=$GDMA_PREFIX/share/man/man1/ INSTALLMAN3DIR=$GDMA_PREFIX/share/man/man3/
make
make install
cp -rf ../GDMA $GDMALIB
chown -R gdma.gdma /usr/local/groundwork/gdma

cd ..
tar czvf /root/gdma_$osname$osmaj.tar.gz /usr/local/groundwork/gdma
