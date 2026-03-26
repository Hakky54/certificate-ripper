#!/usr/bin/env bash
#
# Builds a Debian source package for Launchpad PPA upload.
# The package ships pre-built native binaries for amd64 and arm64.
#
# Usage: ./scripts/build-source-package.sh <version> <distro> <gpg_key_id> [include_orig]
#
# The optional include_orig flag (true/false, default true) controls whether the
# orig tarball is included in the upload (-sa) or omitted (-sd). Launchpad requires
# the orig tarball to be identical across all uploads for the same upstream version,
# so only the first distro upload should include it; subsequent distros should set
# this to "false" so Launchpad reuses the tarball already on file.
#
# Example: ./scripts/build-source-package.sh 2.7.1 noble ABC123DEF456 true
#          ./scripts/build-source-package.sh 2.7.1 jammy ABC123DEF456 false

set -euo pipefail

VERSION="${1:?Usage: build-source-package.sh <version> <distro> <gpg_key_id> [include_orig]}"
DISTRO="${2:?Usage: build-source-package.sh <version> <distro> <gpg_key_id> [include_orig]}"
GPG_KEY_ID="${3:?Usage: build-source-package.sh <version> <distro> <gpg_key_id> [include_orig]}"
INCLUDE_ORIG="${4:-true}"

PACKAGE_NAME="crip"
SOURCE_DIR="${PACKAGE_NAME}-${VERSION}"
ORIG_TARBALL="${PACKAGE_NAME}_${VERSION}.orig.tar.gz"

echo "==> Building source package for ${PACKAGE_NAME} ${VERSION} (${DISTRO})"

# Create source directory with pre-built binaries
mkdir -p "${SOURCE_DIR}/binaries"
cp binary/amd64/crip "${SOURCE_DIR}/binaries/crip-amd64"
cp binary/aarch64/crip "${SOURCE_DIR}/binaries/crip-arm64"

# Reuse existing orig tarball if present (all distros must share the same one for Launchpad)
if [ ! -f "${ORIG_TARBALL}" ]; then
  echo "==> Creating orig tarball ${ORIG_TARBALL}"
  tar -czf "${ORIG_TARBALL}" "${SOURCE_DIR}"
fi

# Create debian packaging directory
mkdir -p "${SOURCE_DIR}/debian/source"

# debian/source/format
echo "3.0 (quilt)" > "${SOURCE_DIR}/debian/source/format"

# debian/compat
echo "12" > "${SOURCE_DIR}/debian/compat"

# debian/control
cat > "${SOURCE_DIR}/debian/control" << 'EOF'
Source: crip
Section: utils
Priority: optional
Maintainer: Hakan Altindag <hakangoudberg@hotmail.com>
Build-Depends: debhelper (>= 12)
Standards-Version: 4.5.1
Homepage: https://github.com/Hakky54/certificate-ripper

Package: crip
Architecture: amd64 arm64
Depends: ${misc:Depends}
Description: CLI tool to extract server certificates
 Certificate Ripper is a CLI tool to extract server certificates from
 HTTPS, WSS, FTPS, IMAPS, and SMTPS servers. Extracted certificates
 can be printed in human-readable or PEM format, and exported to
 PKCS12, JKS, DER, or PEM files.
EOF

# debian/rules
cat > "${SOURCE_DIR}/debian/rules" << 'RULES'
#!/usr/bin/make -f
%:
	dh $@

override_dh_auto_build:
	# Nothing to build, we ship pre-compiled native binaries

override_dh_auto_install:
	install -D -m 755 binaries/crip-$(DEB_HOST_ARCH) debian/crip/usr/bin/crip

override_dh_strip:
	# Skip stripping, binary is a GraalVM native-image

override_dh_shlibdeps:
	# Skip shared library dependency detection
RULES
chmod 755 "${SOURCE_DIR}/debian/rules"

# debian/changelog
cat > "${SOURCE_DIR}/debian/changelog" << EOF
crip (${VERSION}-1~${DISTRO}) ${DISTRO}; urgency=medium

  * Release ${VERSION}

 -- ${DEBFULLNAME} <${DEBEMAIL}>  $(date -R)
EOF

# debian/copyright
cat > "${SOURCE_DIR}/debian/copyright" << 'EOF'
Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
Upstream-Name: certificate-ripper
Upstream-Contact: Hakan Altindag <hakangoudberg@hotmail.com>
Source: https://github.com/Hakky54/certificate-ripper

Files: *
Copyright: 2021 Thunderberry
License: Apache-2.0

License: Apache-2.0
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 .
     https://www.apache.org/licenses/LICENSE-2.0
 .
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
EOF

# debian/source/lintian-overrides (pre-built native binaries have no source)
cat > "${SOURCE_DIR}/debian/source/lintian-overrides" << 'EOF'
crip source: source-is-missing [binaries/crip-amd64]
crip source: source-is-missing [binaries/crip-arm64]
EOF

# Build the unsigned source package (-d skips build dependency checks since we ship pre-built binaries)
# -sa includes the orig tarball (for the first distro upload)
# -sd omits it (for subsequent distros, Launchpad reuses the one already uploaded)
if [ "${INCLUDE_ORIG}" = "true" ]; then
  SA_FLAG="-sa"
else
  SA_FLAG="-sd"
fi

cd "${SOURCE_DIR}"
debuild -S ${SA_FLAG} -d -us -uc
cd ..

# Sign the source package (gpg-agent has the passphrase pre-cached from the workflow)
debsign -k"${GPG_KEY_ID}" "${PACKAGE_NAME}_${VERSION}-1~${DISTRO}_source.changes"

echo "==> Source package built successfully for ${DISTRO}"

