/*
 * Copyright (c) 2016 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.eilslabs.tools.logging

import de.dkfz.eilslabs.tools.logging.IOHelperMethods
import groovy.transform.CompileStatic
import org.junit.Test

/**
 * Test class to cover RoddyIOHelperMethods.
 *
 * Created by heinold on 11.11.15.
 */
@CompileStatic
public class IOHelperMethodsTest {

    @Test
    public void testGetMD5OfText() {
        assert IOHelperMethods.getMD5OfText("ABCD") == "cb08ca4a7bb5f9683c19133a84872ca7";
    }
    /*
    @Test
    public void testGetMD5OfFile() {
        String md5
        File testFile
        try {
            File testBaseDir = MockupExecutionContextBuilder.getDirectory(IOHelperMethodsTest.name, "testGetMD5OfFile");
            testFile = new File(testBaseDir, "A");
            testFile << "ABCD";
            md5 = IOHelperMethods.getMD5OfFile(testFile);
        } finally {
            testFile?.delete();
        }
        assert md5 == "cb08ca4a7bb5f9683c19133a84872ca7";
    }*/
    /*
    private File getTestBaseDir() {
        File testBaseDir = MockupExecutionContextBuilder.getDirectory(IOHelperMethodsTest.name, "testGetSingleMD5OfFilesInDirectory");
        testBaseDir
    }*/

    private List<String> getMD5OfFilesInDirectories(File testBaseDir, File md5TestDir, List<String> filenames) {
        String md5TestDirShort = (md5TestDir.absolutePath - testBaseDir.absolutePath)[1 .. -1];
        return filenames.collect {
            File f = new File(md5TestDir, it)
            f << it
            return IOHelperMethods.getMD5OfText("${md5TestDirShort}/${it}") + IOHelperMethods.getMD5OfFile(f)
        }
    }
    /*
    @Test
    public void testGetSingleMD5OfFilesInDifferentDirectories() {
        File testBaseDir = MockupExecutionContextBuilder.getDirectory(IOHelperMethodsTest.name, "testGetSingleMD5OfFilesInDirectory");
        File md5TestDir1 = new File(testBaseDir, "md5sumtest1");
        File md5TestDir2 = new File(md5TestDir1, "md5sumtest2");
        md5TestDir1.mkdirs();
        md5TestDir2.mkdirs();

        assert getMD5OfFilesInDirectories(testBaseDir, md5TestDir1, ["A", "B"]).join(Constants.ENV_LINESEPARATOR) != getMD5OfFilesInDirectories(testBaseDir, md5TestDir2, ["A", "B"]).join(Constants.ENV_LINESEPARATOR)
    }*/
    /*
    @Test
    public void testGetSingleMD5OfFilesInDirectory() {
        File testBaseDir = getTestBaseDir()
        File md5TestDir = new File(testBaseDir, "md5sumtest");
        File md5TestSubDir = new File(md5TestDir, "sub");
        md5TestSubDir.mkdirs();

        List<String> aList = getMD5OfFilesInDirectories(testBaseDir, md5TestDir, ["A", "B", "C", "D"])
        aList += getMD5OfFilesInDirectories(testBaseDir, md5TestSubDir, ["E", "F"]);

        String text = aList.join(Constants.ENV_LINESEPARATOR)
        assert IOHelperMethods.getSingleMD5OfFilesInDirectory(md5TestDir) == IOHelperMethods.getMD5OfText(text);
    }*/

    /*@Test
    public void testCopyDirectory() {
        File base = MockupExecutionContextBuilder.getDirectory(IOHelperMethodsTest.class.name, "copyDirectory")
        File src = new File(base, "src");
        File dst = new File(base, "dst");
        File dst2 = new File(dst, "dst")

        String nonExecutableButWritable = "nonExecutableButWritable"
        String executableButNotWritable = "executableButNotWritable"

        src.mkdirs();

        File nebw = new File(src, nonExecutableButWritable)
        nebw << "a"
        nebw.setExecutable(false)
        nebw.setWritable(true)

        File exbnw = new File(src, executableButNotWritable)
        exbnw << "b"
        exbnw.setExecutable(true);
        exbnw.setWritable(false)

        assert !nebw.canExecute()
        assert nebw.canWrite()


        assert exbnw.canExecute()
        assert !exbnw.canWrite()

        // To non existing directory with new name
        IOHelperMethods.copyDirectory(src, dst)
        assert dst.exists()
        File nebw2 = new File(dst, nonExecutableButWritable)
        assert !nebw2.canExecute()
        assert nebw2.canWrite()

        File exbnw2 = new File(dst, executableButNotWritable)
        assert exbnw2.canExecute()
        assert !exbnw2.canWrite()

        // To existing directory without new name
        IOHelperMethods.copyDirectory(src, dst2)
        assert dst2.exists()
        File nebw3 = new File(dst2, nonExecutableButWritable)
        assert !nebw3.canExecute()
        assert nebw3.canWrite()

        File exbnw3 = new File(dst2, executableButNotWritable)
        assert exbnw3.canExecute()
        assert !exbnw3.canWrite()

    }*/

    /*
    @Test
    public void testSymbolicToNumericAccessRights() throws Exception {
        FileSystemAccessProvider.resetFileSystemAccessProvider(new FileSystemAccessProvider() {
            @Override
            int getDefaultUserMask() {
                return 0022; // Mock this value to a default value. This might otherwise change from system to system.    rwx,r,r
            }
        });

        Map<String, String> valuesAndExpectedMap = [
                "u=rwx,g=rwx,o=rwx": "0777", //rwx,rwx,rwx
                "u=rwx,g=rwx,o-rwx": "0770", //rwx,rwx,---
                "u+rwx,g+rwx,o-rwx": "0770", //rwx,rwx,---
                "u+rw,g-rw,o-rwx"  : "0710", //rwx,---,---
                "u+rw,g+rw,o-rwx"  : "0770", //rwx,rw-,---
                "u+rw,g+rw"        : "0775", //rwx,rw-,r--
                "u-w,g+rw,u-r"     : "0175", //--x,rwx,r-x  Careful here, u ist set two times!
        ]

        valuesAndExpectedMap.each {
            String rights, String res ->
                assert res == IOHelperMethods.symbolicToNumericAccessRights(rights);
        }
    }
*/
    @Test
    public void testConvertUMaskToAccessRights() throws Exception {
        Map<String, String> valuesAndResults = [
                "0000": "0777",
                "0007": "0770",
                "0067": "0710",
                "0002": "0775",
                "0602": "0175",
        ]

        valuesAndResults.each {
            String rights, String res ->
                assert res == IOHelperMethods.convertUMaskToAccessRights(rights);
        }
    }
}