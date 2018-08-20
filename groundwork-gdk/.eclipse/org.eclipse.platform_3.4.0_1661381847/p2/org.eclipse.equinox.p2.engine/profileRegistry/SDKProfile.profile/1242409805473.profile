<?xml version='1.0' encoding='UTF-8'?>
<?profile class='org.eclipse.equinox.internal.p2.engine.Profile' version='0.0.2'?>
<profile id='SDKProfile' timestamp='1242409805473'>
  <properties size='13'>
    <property name='org.eclipse.equinox.p2.cache' value='/home/nagios/.eclipse/org.eclipse.platform_3.4.0_1661381847'/>
    <property name='org.eclipse.equinox.p2.flavor' value='tooling'/>
    <property name='org.eclipse.equinox.p2.roaming' value='false'/>
    <property name='org.eclipse.update.install.features' value='true'/>
    <property name='org.eclipse.equinox.p2.environments' value='osgi.ws=gtk,osgi.os=linux,osgi.arch=x86'/>
    <property name='org.eclipse.equinox.p2.installFolder' value='/usr/local/groundwork/gdk/tools/eclipse'/>
    <property name='eclipse.touchpoint.launcherName' value='eclipse'/>
    <property name='org.eclipse.equinox.p2.surrogate' value='true'/>
    <property name='org.eclipse.equinox.p2.shared.timestamp' value='1234398407184'/>
    <property name='org.eclipse.equinox.p2.cache.shared' value='/usr/local/groundwork/gdk/tools/eclipse'/>
    <property name='org.eclipse.equinox.p2.configurationFolder' value='/home/nagios/.eclipse/org.eclipse.platform_3.4.0_1661381847/configuration'/>
    <property name='org.eclipse.equinox.p2.launcherConfiguration' value='/home/nagios/.eclipse/org.eclipse.platform_3.4.0_1661381847/configuration/eclipse.ini.ignored'/>
    <property name='org.eclipse.equinox.p2.cache.extensions' value='file:/usr/local/groundwork/gdk/tools/eclipse/.eclipseextension|file:/home/nagios/.eclipse/org.eclipse.platform_3.4.0_1661381847/configuration/org.eclipse.osgi/bundles/68/data/listener_1925729951/'/>
  </properties>
  <units size='4'>
    <unit id='org.eclipse.sdk.ide.launcher.gtk.linux.x86' version='3.4.2.M20090211-1700'>
      <provides size='2'>
        <provided namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.gtk.linux.x86' version='3.4.2.M20090211-1700'/>
        <provided namespace='toolingorg.eclipse.sdk.ide' name='org.eclipse.sdk.ide.launcher' version='3.4.2.M20090211-1700'/>
      </provides>
      <requires size='1'>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.equinox.launcher.gtk.linux.x86' range='0.0.0'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
      </requires>
      <filter>
        (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86))
      </filter>
      <artifacts size='1'>
        <artifact classifier='binary' id='org.eclipse.sdk.ide.launcher.gtk.linux.x86' version='3.4.2.M20090211-1700'/>
      </artifacts>
      <touchpoint id='org.eclipse.equinox.p2.native' version='1.0.0'/>
    </unit>
    <unit id='toolingorg.eclipse.sdk.ide.launcher.gtk.linux.x86' version='3.4.2.M20090211-1700' singleton='false'>
      <hostRequirements size='1'>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.gtk.linux.x86' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'/>
      </hostRequirements>
      <properties size='1'>
        <property name='org.eclipse.equinox.p2.type.fragment' value='true'/>
      </properties>
      <provides size='1'>
        <provided namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.gtk.linux.x86' version='3.4.2.M20090211-1700'/>
      </provides>
      <requires size='1'>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.gtk.linux.x86' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'/>
      </requires>
      <filter>
        (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86))
      </filter>
      <touchpoint id='org.eclipse.equinox.p2.native' version='1.0.0'/>
      <touchpointData size='1'>
        <instructions size='2'>
          <instruction key='uninstall'>
            cleanupzip(source:@artifact, target:${installFolder});
          </instruction>
          <instruction key='install'>
            unzip(source:@artifact, target:${installFolder}); chmod(targetDir:${installFolder}, targetFile:notice.html, permissions:755); chmod(targetDir:${installFolder}, targetFile:configuration, permissions:755); chmod(targetDir:${installFolder}, targetFile:libcairo-swt.so, permissions:755); chmod(targetDir:${installFolder}, targetFile:readme, permissions:755); chmod(targetDir:${installFolder}, targetFile:about_files, permissions:755); chmod(targetDir:${installFolder}, targetFile:about.html, permissions:755); chmod(targetDir:${installFolder}, targetFile:eclipse, permissions:755); chmod(targetDir:${installFolder}, targetFile:.eclipseproduct, permissions:755); chmod(targetDir:${installFolder}, targetFile:eclipse.ini, permissions:755); chmod(targetDir:${installFolder}, targetFile:epl-v10.html, permissions:755);
          </instruction>
        </instructions>
      </touchpointData>
    </unit>
    <unit id='org.eclipse.sdk.ide' version='3.4.2.M20090211-1700'>
      <update id='org.eclipse.sdk.ide' range='0.0.0' severity='0'/>
      <properties size='3'>
        <property name='org.eclipse.equinox.p2.name' value='Eclipse SDK'/>
        <property name='lineUp' value='true'/>
        <property name='org.eclipse.equinox.p2.type.group' value='true'/>
      </properties>
      <provides size='1'>
        <provided namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide' version='3.4.2.M20090211-1700'/>
      </provides>
      <requires size='85'>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.ppcorg.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.gtk.linux.ppc' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.motif.linux.x86.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=linux)(osgi.ws=motif)(osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.aix.ppcorg.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=aix) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwin32.win32.x86org.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.carbon.macosx.ppc.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=macosx)(osgi.ws=carbon)(osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.linux.x86org.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.x86org.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.solaris.sparcorg.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=solaris) (osgi.arch=sparc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingcarbon.macosx.ppcorg.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.x86_64org.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.equinox.launcher' range='[1.0.101.R34x_v20081125,1.0.101.R34x_v20081125]'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.ppcorg.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.motif.hpux.ia64_32' range='[1.0.2.R34x_v20081125,1.0.2.R34x_v20081125]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=hpux) (osgi.arch=ia64_32) )
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.carbon.macosx.ppc' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwin32.win32.x86_64org.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.linux.x86org.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.gtk.linux.x86' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingcarbon.macosx.ppcorg.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.hpux.ia64_32org.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=hpux) (osgi.arch=ia64_32))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.x86org.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.gtk.linux.x86.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=linux)(osgi.ws=gtk)(osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingcarbon.macosx.x86org.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.carbon.macosx.x86' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.gtk.solaris.sparc' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=solaris) (osgi.arch=sparc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.carbon.macosx.x86.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=macosx)(osgi.ws=carbon)(osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwpf.win32.x86org.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=wpf) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher' range='[1.0.101.R34x_v20081125,1.0.101.R34x_v20081125]'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.gtk.linux.x86_64' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.win32.win32.x86' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.simpleconfigurator' range='[1.0.0.v20080604,1.0.0.v20080604]'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwpf.win32.x86org.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=wpf) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.x86_64org.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwin32.win32.x86org.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.carbon.macosx' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (|(osgi.arch=x86)(osgi.arch=ppc)) )
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.gtk.linux.x86_64.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=linux)(osgi.ws=gtk)(osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.motif.aix.ppc.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=aix)(osgi.ws=motif)(osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwin32.win32.x86_64org.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.win32.win32.x86_64' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.hpux.ia64_32org.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=hpux) (osgi.arch=ia64_32))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.x86org.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwpf.win32.x86org.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=wpf) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingcarbon.macosx.x86org.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingcarbon.macosx.ppcorg.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.win32.win32.x86' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.gtk.linux.ppc' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.gtk.linux.x86_64' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.win32.win32.x86_64' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.motif.hpux.ia64_32' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=hpux) (osgi.arch=ia64_32))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.motif.aix.ppc' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=aix) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.aix.ppcorg.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=aix) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.wpf.win32.x86.eclipse.exe' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=win32)(osgi.ws=wpf)(osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwin32.win32.x86_64org.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.gtk.solaris.sparc' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=solaris) (osgi.arch=sparc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.equinox.p2.user.ui.feature.group' range='[1.0.2.r34x_v20090120-7d-7tEQcCaaYSBeNOClOn02267,1.0.2.r34x_v20090120-7d-7tEQcCaaYSBeNOClOn02267]'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.motif.aix.ppc' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=aix) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.wpf.win32.x86' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=wpf) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingcarbon.macosx.x86org.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=carbon) (osgi.os=macosx) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.motif.hpux.ia64_32.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=hpux)(osgi.ws=motif)(osgi.arch=ia64_32))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.gtk.solaris.sparc.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=solaris)(osgi.ws=gtk)(osgi.arch=sparc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingwin32.win32.x86org.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=win32) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.wpf.win32.x86' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=wpf) (osgi.os=win32) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.solaris.sparcorg.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=solaris) (osgi.arch=sparc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.sdk.ide.launcher.motif.linux.x86' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.win32.win32.x86_64.eclipse.exe' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=win32)(osgi.ws=win32)(osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.feature.group' range='[3.4.2.R342_v20090122-7O7S7GApJ3_vCyk7ETmsfcmjhz0eHnqw7MIjk9Vdhe4Ic,3.4.2.R342_v20090122-7O7S7GApJ3_vCyk7ETmsfcmjhz0eHnqw7MIjk9Vdhe4Ic]'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.motif.hpux.PA_RISC' range='[1.0.100.v20080303,1.0.100.v20080303]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=hpux) (osgi.arch=PA_RISC))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.linux.x86org.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.aix.ppcorg.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=aix) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.motif.linux.x86' range='[1.0.101.R34x_v20080805,1.0.101.R34x_v20080805]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.solaris.sparcorg.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=solaris) (osgi.arch=sparc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.x86_64org.eclipse.equinox.common' range='[3.4.0.v20080421-2006,3.4.0.v20080421-2006]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86_64))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.p2.reconciler.dropins' range='[1.0.4.v20081027-2115,1.0.4.v20081027-2115]'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingmotif.hpux.ia64_32org.eclipse.core.runtime' range='[3.4.0.v20080512,3.4.0.v20080512]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=hpux) (osgi.arch=ia64_32))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.motif.solaris.sparc' range='[1.0.101.R34x_v20080731,1.0.101.R34x_v20080731]'>
          <filter>
            (&amp; (osgi.ws=motif) (osgi.os=solaris) (osgi.arch=sparc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.win32.win32.x86.eclipse.exe' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=win32)(osgi.ws=win32)(osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.sdk.ide.launcher.gtk.linux.ppc.eclipse' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'>
          <filter>
            (&amp; (osgi.os=linux)(osgi.ws=gtk)(osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolingorg.eclipse.equinox.launcher.gtk.linux.x86' range='[1.0.101.R34x_v20080805,1.0.101.R34x_v20080805]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=x86))
          </filter>
        </required>
        <required namespace='org.eclipse.equinox.p2.iu' name='toolinggtk.linux.ppcorg.eclipse.update.configurator' range='[3.2.201.R34x_v20080819,3.2.201.R34x_v20080819]'>
          <filter>
            (&amp; (osgi.ws=gtk) (osgi.os=linux) (osgi.arch=ppc))
          </filter>
        </required>
        <required namespace='toolingorg.eclipse.sdk.ide' name='org.eclipse.sdk.ide.launcher' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]' multiple='true'/>
        <required namespace='toolingorg.eclipse.sdk.ide' name='org.eclipse.sdk.ide.ini' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'/>
        <required namespace='toolingorg.eclipse.sdk.ide' name='org.eclipse.sdk.ide.config' range='[3.4.2.M20090211-1700,3.4.2.M20090211-1700]'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='tooling.osgi.bundle.default' range='0.0.0'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='tooling.source.default' range='0.0.0'/>
        <required namespace='org.eclipse.equinox.p2.iu' name='tooling.org.eclipse.update.feature.default' range='0.0.0' optional='true'>
          <filter>
            (org.eclipse.update.install.features=true)
          </filter>
        </required>
      </requires>
      <touchpoint id='org.eclipse.equinox.p2.osgi' version='1.0.0'/>
      <touchpointData size='1'>
        <instructions size='2'>
          <instruction key='configure'>

          </instruction>
          <instruction key='unconfigure'>

          </instruction>
        </instructions>
      </touchpointData>
    </unit>
    <unit id='a.jre' version='1.6.0' singleton='false'>
      <provides size='117'>
        <provided namespace='org.eclipse.equinox.p2.iu' name='a.jre' version='1.6.0'/>
        <provided namespace='java.package' name='javax.accessibility' version='0.0.0'/>
        <provided namespace='java.package' name='javax.activity' version='0.0.0'/>
        <provided namespace='java.package' name='javax.crypto' version='0.0.0'/>
        <provided namespace='java.package' name='javax.crypto.interfaces' version='0.0.0'/>
        <provided namespace='java.package' name='javax.crypto.spec' version='0.0.0'/>
        <provided namespace='java.package' name='javax.imageio' version='0.0.0'/>
        <provided namespace='java.package' name='javax.imageio.event' version='0.0.0'/>
        <provided namespace='java.package' name='javax.imageio.metadata' version='0.0.0'/>
        <provided namespace='java.package' name='javax.imageio.plugins.bmp' version='0.0.0'/>
        <provided namespace='java.package' name='javax.imageio.plugins.jpeg' version='0.0.0'/>
        <provided namespace='java.package' name='javax.imageio.spi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.imageio.stream' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management.loading' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management.modelmbean' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management.monitor' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management.openmbean' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management.relation' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management.remote' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management.remote.rmi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.management.timer' version='0.0.0'/>
        <provided namespace='java.package' name='javax.naming' version='0.0.0'/>
        <provided namespace='java.package' name='javax.naming.directory' version='0.0.0'/>
        <provided namespace='java.package' name='javax.naming.event' version='0.0.0'/>
        <provided namespace='java.package' name='javax.naming.ldap' version='0.0.0'/>
        <provided namespace='java.package' name='javax.naming.spi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.net' version='0.0.0'/>
        <provided namespace='java.package' name='javax.net.ssl' version='0.0.0'/>
        <provided namespace='java.package' name='javax.print' version='0.0.0'/>
        <provided namespace='java.package' name='javax.print.attribute' version='0.0.0'/>
        <provided namespace='java.package' name='javax.print.attribute.standard' version='0.0.0'/>
        <provided namespace='java.package' name='javax.print.event' version='0.0.0'/>
        <provided namespace='java.package' name='javax.rmi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.rmi.CORBA' version='0.0.0'/>
        <provided namespace='java.package' name='javax.rmi.ssl' version='0.0.0'/>
        <provided namespace='java.package' name='javax.security.auth' version='0.0.0'/>
        <provided namespace='java.package' name='javax.security.auth.callback' version='0.0.0'/>
        <provided namespace='java.package' name='javax.security.auth.kerberos' version='0.0.0'/>
        <provided namespace='java.package' name='javax.security.auth.login' version='0.0.0'/>
        <provided namespace='java.package' name='javax.security.auth.spi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.security.auth.x500' version='0.0.0'/>
        <provided namespace='java.package' name='javax.security.cert' version='0.0.0'/>
        <provided namespace='java.package' name='javax.security.sasl' version='0.0.0'/>
        <provided namespace='java.package' name='javax.sound.midi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.sound.midi.spi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.sound.sampled' version='0.0.0'/>
        <provided namespace='java.package' name='javax.sound.sampled.spi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.sql' version='0.0.0'/>
        <provided namespace='java.package' name='javax.sql.rowset' version='0.0.0'/>
        <provided namespace='java.package' name='javax.sql.rowset.serial' version='0.0.0'/>
        <provided namespace='java.package' name='javax.sql.rowset.spi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.border' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.colorchooser' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.event' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.filechooser' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.plaf' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.plaf.basic' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.plaf.metal' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.plaf.multi' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.plaf.synth' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.table' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.text' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.text.html' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.text.html.parser' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.text.rtf' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.tree' version='0.0.0'/>
        <provided namespace='java.package' name='javax.swing.undo' version='0.0.0'/>
        <provided namespace='java.package' name='javax.transaction' version='0.0.0'/>
        <provided namespace='java.package' name='javax.transaction.xa' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.datatype' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.namespace' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.parsers' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.transform' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.transform.dom' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.transform.sax' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.transform.stream' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.validation' version='0.0.0'/>
        <provided namespace='java.package' name='javax.xml.xpath' version='0.0.0'/>
        <provided namespace='java.package' name='org.ietf.jgss' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CORBA' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CORBA_2_3' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CORBA_2_3.portable' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CORBA.DynAnyPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CORBA.ORBPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CORBA.portable' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CORBA.TypeCodePackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CosNaming' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CosNaming.NamingContextExtPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.CosNaming.NamingContextPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.Dynamic' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.DynamicAny' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.DynamicAny.DynAnyFactoryPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.DynamicAny.DynAnyPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.IOP' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.IOP.CodecFactoryPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.IOP.CodecPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.Messaging' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.PortableInterceptor' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.PortableInterceptor.ORBInitInfoPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.PortableServer' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.PortableServer.CurrentPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.PortableServer.POAManagerPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.PortableServer.POAPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.PortableServer.portable' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.PortableServer.ServantLocatorPackage' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.SendingContext' version='0.0.0'/>
        <provided namespace='java.package' name='org.omg.stub.java.rmi' version='0.0.0'/>
        <provided namespace='java.package' name='org.w3c.dom' version='0.0.0'/>
        <provided namespace='java.package' name='org.w3c.dom.bootstrap' version='0.0.0'/>
        <provided namespace='java.package' name='org.w3c.dom.events' version='0.0.0'/>
        <provided namespace='java.package' name='org.w3c.dom.ls' version='0.0.0'/>
        <provided namespace='java.package' name='org.xml.sax' version='0.0.0'/>
        <provided namespace='java.package' name='org.xml.sax.ext' version='0.0.0'/>
        <provided namespace='java.package' name='org.xml.sax.helpers' version='0.0.0'/>
      </provides>
      <touchpoint id='org.eclipse.equinox.p2.native' version='1.0.0'/>
    </unit>
  </units>
  <iusProperties size='4'>
    <iuProperties id='org.eclipse.sdk.ide.launcher.gtk.linux.x86' version='3.4.2.M20090211-1700'>
      <properties size='3'>
        <property name='unzipped|@artifact|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse' value='/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/notice.html|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/configuration/config.ini|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/configuration/.settings/org.eclipse.equinox.p2.metadata.repository.prefs|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/configuration/.settings/org.eclipse.equinox.p2.artifact.repository.prefs|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/libcairo-swt.so|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/readme/readme_eclipse.html|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/about_files/mpl-v11.txt|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/about_files/IJG_README|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/about_files/pixman-licenses.txt|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/about_files/about_cairo.html|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/about_files/lgpl-v21.txt|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/about.html|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/eclipse|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/.eclipseproduct|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/eclipse.ini|/builds/M/src/M20090211-1700/p2temp/equinox.p2.build/sdk.install.linux.gtk.x86/eclipse/epl-v10.html|'/>
        <property name='org.eclipse.equinox.p2.type.lock' value='3'/>
        <property name='org.eclipse.equinox.p2.base' value='true'/>
      </properties>
    </iuProperties>
    <iuProperties id='toolingorg.eclipse.sdk.ide.launcher.gtk.linux.x86' version='3.4.2.M20090211-1700'>
      <properties size='2'>
        <property name='org.eclipse.equinox.p2.type.lock' value='3'/>
        <property name='org.eclipse.equinox.p2.base' value='true'/>
      </properties>
    </iuProperties>
    <iuProperties id='org.eclipse.sdk.ide' version='3.4.2.M20090211-1700'>
      <properties size='4'>
        <property name='org.eclipse.equinox.p2.type.root' value='true'/>
        <property name='org.eclipse.equinox.p2.internal.inclusion.rules' value='STRICT'/>
        <property name='org.eclipse.equinox.p2.type.lock' value='3'/>
        <property name='org.eclipse.equinox.p2.base' value='true'/>
      </properties>
    </iuProperties>
    <iuProperties id='a.jre' version='1.6.0'>
      <properties size='2'>
        <property name='org.eclipse.equinox.p2.type.lock' value='3'/>
        <property name='org.eclipse.equinox.p2.base' value='true'/>
      </properties>
    </iuProperties>
  </iusProperties>
</profile>
