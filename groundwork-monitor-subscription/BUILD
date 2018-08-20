Here is how to build these former-Factory modules:

================================================================
Preparation
================================================================

Before building, you must check out the code from under the

    http://geneva/groundwork-professional/trunk/monitor-subscription/

directory, for the modules you wish to build.

Secondly, this checkout MUST be done on the machine you wish to build
the modules on.  That machine MUST have Subversion access, as it will
be needed during the RPM build process.  We embed the current Subversion
version number into the package release string (e.g., "-11431" in the
"4.0.0-11431" release for the groundwork-fping-feeder RPM), as a means
of automatically and unambiguously recording exactly what code went
into making the constructed RPM.

Thirdly, you MUST NOT make any changes to the code which are not checked
into Subversion before constructing an RPM for public release.  That would
subvert the clear signal in the Subversion release number of exactly what
code was used to construct the RPM.  It is okay to make changes for your
own testing, but any such changes must be checked into Subversion before
releasing a copy of the RPM to any other party.  And in particular,
it is VITAL that such checkins be done BEFORE any RPMs are built that
will be subsequently checked into the */archives/rpms/ directories here.

================================================================
Building RPMs
================================================================

For each module, descend into that directory and then execute just
two simple commands to construct the entire RPM.  For example:

    cd fping-feeder;     make clean; make rpms
    cd nsca-bulk;        make clean; make rpms
    cd perl-typedconfig; make clean; make rpms

In each case, the constructed RPM will be found in the rpmbuild/RPMS/
subdirectory, such as:

    fping-feeder/rpmbuild/RPMS/groundwork-fping-feeder-4.0.0-11431.noarch.rpm
    nsca-bulk/rpmbuild/RPMS/groundwork-nsca-bulk-2.0.0-11431.noarch.rpm
    perl-typedconfig/rpmbuild/RPMS/groundwork-perl-typedconfig-2.0.0-11431.noarch.rpm

These are transient copies, suitable for testing, which are NOT
to be permanently archived in those locations.  When testing shows
these copies to be properly working, they should be moved into the
associated permanent-archive locations (*/archives/rpms/), and checked
into Subversion at those places.  That will allow other people to grab
known-good pre-built RPMs for immediate use without bothering with their
own builds.  For example, we have:

    fping-feeder/archives/rpms/groundwork-fping-feeder-4.0.0-11431.noarch.rpm
    nsca-bulk/archives/rpms/groundwork-nsca-bulk-2.0.0-11431.noarch.rpm
    perl-typedconfig/archives/rpms/groundwork-perl-typedconfig-2.0.0-11431.noarch.rpm

In the case of many of the former-Factory RPMs, such as those listed
above, the constructed RPMs are of the "noarch" build architecture,
meaning that one build on one platform will suffice to support all
platforms.  If that is not the case for some RPM, it will be necessary
to build on each supported platform separately, and the individual RPMs
should all be archived as above.

================================================================
Versioning
================================================================

It is VERY IMPORTANT that version numbers be correctly rolled when
upgrades are made to these RPMs.  Version numbers are found in at least
the following places:

* each makefile (e.g., FPING_FEEDER_RELEASE in the fping-feeder/makefile)
* in the name of the specfile (e.g.,
    fping-feeder/rpmsetup/groundwork-fping-feeder-4.0.0.spec)
* in these lines in the specfile:

    %define major_release   4
    %define minor_release   0
    %define patch_release   0

* sometimes within scripts, such as:

    my $VERSION = "4.0.0";

and possibly in documentation files as well.  Be thorough in looking
for version numbers and upgrading them, as it can be quite confusing
when we find mismatched values in deployed software.

At a minimum, the patch_release number MUST be rolled for each bug fix.
That means that a new specfile will need to be created even for a trivial
upgrade.  Be sure to add %changelog lines at the end of the new specfile
telling what is new with this release.

Note that, for the transition from the Factory versions of these RPMs
to versions controlled by Engineering, the major_release numbers have
all been bumped up, and the minor_release and patch_release reset to 0.
This should both serve to distinguish the RPMs because of the criticality
of the pathname and dependency changes made in these new versions, and
also provide some room for bug fixes in the old Factory copies of these
RPMs should that become necessary.
