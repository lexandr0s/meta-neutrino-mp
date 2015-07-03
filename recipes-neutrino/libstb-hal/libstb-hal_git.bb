##
## TODO: spark rules are only build tested
##

SUMMARY = "Library to abstract STB hardware. Supports Tripledragon, AZbox ME, Fulan Spark boxes as well as generic PC hardware and the Raspberry Pi right now."
DESCRIPTION = "Library to abstract STB hardware."
HOMEPAGE = "https://github.com/neutrino-mp"
SECTION = "libs"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://${THISDIR}/libstb-hal/COPYING.GPL;md5=751419260aa954499f7abaabaa882bbe"

# hack: make sure we do not try to build on coolstream
COMPATIBLE_MACHINE_coolstream = "none"
#
# this stuff really is machine specific, not CPU specific
PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "\
	openthreads \
	ffmpeg \
"

# on coolstream, the same is provided by cs-drivers pkg (libcoolstream)
PROVIDES += "virtual/stb-hal-libs"

DEPENDS_append_spark = "tdt-driver libass"
DEPENDS_append_raspberrypi = "virtual/egl"
DEPENDS_append_tripledragon = "directfb triple-sdk"

RDEPENDS_${PN} = "ffmpeg"
RDEPENDS_${PN}-dev_append_spark = " tdt-driver-dev"
RDEPENDS_${PN}-dev_append_tripledragon = " triple-sdk-dev"

SRCREV = "${AUTOREV}"
PV = "0.1+git${SRCPV}"

# prepend, or it will end up in -bin package...
PACKAGES_prepend_spark = "spark-fp "
# libstb-hal-bin package for testing binaries etc.
PACKAGES += "${PN}-bin"

#git://github.com/neutrino-mp/libstb-hal.git;protocol=https 


SRC_URI = " \
	git://${libstb-hal_git};protocol=https \
	file://blank_480.mpg \
	file://blank_576.mpg \
	file://timer-wakeup.init \
"

S = "${WORKDIR}/git"

# the build system is not really broken wrt separate builddir,
# but I want it to build inside the source for various reasons :-)
inherit autotools-brokensep pkgconfig

# CFLAGS_append = " -Wall -W -Wshadow -g -O2 -fno-strict-aliasing -rdynamic -DNEW_LIBCURL"

CFLAGS_spark += "-funsigned-char"
CPPFLAGS_tripledragon += "-I${STAGING_DIR_HOST}/usr/include/hardware"

LDFLAGS = " -Wl,-rpath-link,${STAGING_DIR_HOST}/usr/lib -L${STAGING_DIR_HOST}/usr/lib"

EXTRA_OECONF += "\
	--enable-maintainer-mode \
	--disable-silent-rules \
	--enable-shared \
"

EXTRA_OECONF_append_spark += "--with-boxtype=spark"
EXTRA_OECONF_append_raspberrypi += "--with-boxtype=generic --with-boxmodel=raspi"
EXTRA_OECONF_append_tripledragon += "--with-boxtype=tripledragon"

do_configure_prepend() {
	export AUTOMAKE="automake --foreign"
}

do_install_append() {
	install -d ${D}/${datadir}
}

do_install_append_tripledragon() {
	install -D -m 0644 ${WORKDIR}/blank_576.mpg ${D}/${datadir}/tuxbox/blank_576.mpg
	install -D -m 0644 ${WORKDIR}/blank_480.mpg ${D}/${datadir}/tuxbox/blank_480.mpg
}

do_install_append_spark() {
	install -D -m 0755 ${WORKDIR}/timer-wakeup.init ${D}/${sysconfdir}/init.d/timer-wakeup
	# neutrino is 99, so we put this at 98.
	update-rc.d -r ${D} timer-wakeup start 98 5 .
}

# pic2m2v is included in lib package, because it is always needed,
# libstb-hal-bin contains all other binaries, which are rather for testing only
FILES_${PN} = "\
	${libdir}/* \
	${bindir}/pic2m2v \
	${datadir} \
"

FILES_${PN}-dev += "${includedir}/libstb-hal/*"

FILES_spark-fp = " \
	${bindir}/spark_fp \
	${sysconfdir} \
"
