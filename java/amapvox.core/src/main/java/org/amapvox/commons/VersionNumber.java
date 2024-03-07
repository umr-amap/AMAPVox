
package org.amapvox.commons;

/**
 *
 * @author pverley
 */
public class VersionNumber implements Comparable<VersionNumber> {

    final int major;
    final int minor;
    final int patch;

    public VersionNumber(int major, int minor, int patch) {

        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Returns an {@code VersionNumber} object holding the value of the
     * specified {@code String}.
     *
     * @param s the string to be parsed.
     * @return an {@code VersionNumber} object holding the value represented by
     * the string argument.
     * @exception VersionNumberFormatException if the string cannot be parsed as
     * a version number.
     */
    public static VersionNumber valueOf(String s) throws VersionNumberFormatException {

        if (s == null) {
            throw new VersionNumberFormatException("null");
        }

        if (!s.isEmpty()) {
            try {
                int smajor, sminor = 0, spatch = 0;
                String[] numbers = s.split("\\.");
                smajor = Integer.valueOf(numbers[0]);
                if (numbers.length > 1) {
                    sminor = Integer.valueOf(numbers[1]);
                }
                if (numbers.length > 2) {
                    // remove -alpha -beta -snapshot or any suffix to spatch number
                    spatch = Integer.valueOf(numbers[2].split("-")[0]);
                }
                return new VersionNumber(smajor, sminor, spatch);
            } catch (NumberFormatException ex) {
                throw VersionNumberFormatException.forInputString(s);
            }
        } else {
            throw VersionNumberFormatException.forInputString(s);
        }
    }

    @Override
    public int compareTo(VersionNumber otherVersion) {

        // Compare major version number
        if (major != otherVersion.major) {
            return Integer.compare(major, otherVersion.major);
        }

        // Same major version number, compare minor version number
        if (minor != otherVersion.minor) {
            return Integer.compare(minor, otherVersion.minor);
        }

        // Same major/minor version number, compare patch number
        if (patch != otherVersion.patch) {
            return Integer.compare(patch, otherVersion.patch);
        }

        // Same major, minor and patch version numbers, versions are equal
        return 0;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    
}
