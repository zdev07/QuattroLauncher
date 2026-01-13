package net.kdt.pojavlaunch.multirt;

import static net.kdt.pojavlaunch.Tools.NATIVE_LIB_DIR;
import static org.apache.commons.io.FileUtils.listFiles;
import android.system.Os;
import android.util.Log;
import com.kdt.mcgui.ProgressLayout;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.MathUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.util.*;

/** Quattro Multi-Runtime Manager */
public class MultiRTUtils {
    private static final String TAG = "Quattro.MultiRT";
    private static final HashMap<String, Runtime> sCache = new HashMap<>();
    private static final File RUNTIME_FOLDER = new File(Tools.MULTIRT_HOME);
    private static final String JAVA_VERSION_STR = "JAVA_VERSION=\"";
    private static final String OS_ARCH_STR = "OS_ARCH=\"";

    public static List<Runtime> getRuntimes() {
        if(!RUNTIME_FOLDER.exists() && !RUNTIME_FOLDER.mkdirs()) {
            throw new RuntimeException("Quattro: Failed to create runtime directory");
        }

        ArrayList<Runtime> runtimes = new ArrayList<>();
        File[] files = RUNTIME_FOLDER.listFiles();
        if(files != null) {
            for(File f : files) {
                if (f.isDirectory()) runtimes.add(read(f.getName()));
            }
        }
        return runtimes;
    }

    public static void installRuntimeNamed(String nativeLibDir, InputStream runtimeInputStream, String name) throws IOException {
        File dest = new File(RUNTIME_FOLDER, name);
        if(dest.exists()) FileUtils.deleteDirectory(dest);
        uncompressTarXZ(runtimeInputStream, dest);
        runtimeInputStream.close();
        unpack200(nativeLibDir, dest.getAbsolutePath());
        ProgressLayout.clearProgress(ProgressLayout.UNPACK_RUNTIME);
        read(name);
    }

    public static Runtime read(String name) {
        Runtime returnRuntime = sCache.get(name);
        if(returnRuntime != null) return returnRuntime;
        
        File release = new File(RUNTIME_FOLDER, name + "/release");
        if(!release.exists()) return new Runtime(name);

        try {
            String content = Tools.read(release.getAbsolutePath());
            String javaVersion = Tools.extractUntilCharacter(content, JAVA_VERSION_STR, '"');
            String osArch = Tools.extractUntilCharacter(content, OS_ARCH_STR, '"');
            
            if(javaVersion != null && osArch != null) {
                String[] split = javaVersion.split("\\.");
                int major = split[0].equals("1") ? Integer.parseInt(split[1]) : Integer.parseInt(split[0]);
                returnRuntime = new Runtime(name, javaVersion, osArch, major);
            } else {
                returnRuntime = new Runtime(name);
            }
        } catch(IOException e) {
            returnRuntime = new Runtime(name);
        }
        
        sCache.put(name, returnRuntime);
        return returnRuntime;
    }

    private static void unpack200(String nativeLibraryDir, String runtimePath) {
        Collection<File> files = listFiles(new File(runtimePath), new String[]{"pack"}, true);
        ProcessBuilder pb = new ProcessBuilder().directory(new File(nativeLibraryDir));
        
        for(File packFile : files){
            try {
                // Using Quattro-patched unpack200
                Process process = pb.command("./libunpack200.so", "-r", packFile.getAbsolutePath(), packFile.getAbsolutePath().replace(".pack", "")).start();
                process.waitFor();
            } catch (InterruptedException | IOException e) {
                Log.e(TAG, "Failed to unpack: " + packFile.getName());
            }
        }
    }

    private static void uncompressTarXZ(InputStream is, File dest) throws IOException {
        net.kdt.pojavlaunch.utils.FileUtils.ensureDirectory(dest);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new XZCompressorInputStream(is));
        TarArchiveEntry entry;
        while ((entry = tarIn.getNextTarEntry()) != null) {
            ProgressLayout.setProgress(ProgressLayout.UNPACK_RUNTIME, 100, R.string.global_unpacking, entry.getName());
            File destPath = new File(dest, entry.getName());
            net.kdt.pojavlaunch.utils.FileUtils.ensureParentDirectory(destPath);
            if (entry.isSymbolicLink()) {
                try { Os.symlink(entry.getLinkName(), destPath.getAbsolutePath()); } catch (Exception e) { Log.e(TAG, "Symlink failed", e); }
            } else if (entry.isDirectory()) {
                net.kdt.pojavlaunch.utils.FileUtils.ensureDirectory(destPath);
            } else {
                try (FileOutputStream os = new FileOutputStream(destPath)) {
                    IOUtils.copyLarge(tarIn, os);
                }
            }
        }
        tarIn.close();
    }
}
