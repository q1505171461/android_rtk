package com.example.fortest;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Utils {
    private static final String TAG = "FileUtils";

    public static boolean copyAssetsToStorage(Context context, String assetsPath, String storagePath) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] files = assetManager.list(assetsPath);
            if (files == null){
                return true;
            }
            for (String filename : files) {
                Log.i(TAG, " filename = " + filename);
                String assetPath = assetsPath + File.separator + filename;
                String storageFilePath = storagePath + File.separator ;
                if (Objects.requireNonNull(assetManager.list(assetPath)).length == 0) {
                    // It's a file
                    copyAssetFile(context, assetPath, storageFilePath,filename);
                } else {
                    // It's a directory
                    File directory = new File(storageFilePath+filename);
                    if (!directory.exists()) {
                        Log.i(TAG, "Creating directory: " + directory.getPath());
                        if (!directory.mkdirs()) {
                            Log.e(TAG, "Failed to create directory: " + directory.getPath());
                            return false;
                        }
                    }
                    copyAssetsToStorage(context, assetPath, storageFilePath+filename);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error copying assets", e);
        }
        return true;
    }

    private static void copyAssetFile(Context context, String assetFilePath, String storageFilePath, String fname) {
        Pattern p_fname = Pattern.compile("[^\"]+\\.json$");
        if (p_fname.matcher(fname).matches()){
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(assetFilePath)));
                 OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(storageFilePath+fname))) {
                String buffer ;
                Pattern p_jsonfname = Pattern.compile(".+([^\"]+\\.json).+");
                while ((buffer = reader.readLine()) != null) {
                    if (p_jsonfname.matcher(buffer).matches()){
                        buffer = Pattern.compile("([^\"]+\\.json)").matcher(buffer).replaceAll(storageFilePath+"$1");
                        writer.write(buffer + "\n");
                    }else{
                        writer.write(buffer+"\n");
                    }
                }
                Log.d(TAG, "Copied " + assetFilePath + " to " + storageFilePath);
            } catch (IOException e) {
                Log.e(TAG, "Error copying asset file: " + assetFilePath, e);
            }
        }else{
            try (InputStream in = context.getAssets().open(assetFilePath);
                 OutputStream out = new FileOutputStream(storageFilePath+fname)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                Log.d(TAG, "Copied " + assetFilePath + " to " + storageFilePath);
            } catch (IOException e) {
                Log.e(TAG, "Error copying asset file: " + assetFilePath, e);
            }
        }
    }
    public static double[] xyz2blh(double[] x, double scale, double a0, double b0, double dx, double dy, double dz) {
        double a, b;
        if (a0 == 0.0 || b0 == 0) {
            a = 6378137.0;
            b = 298.257223563;
        } else {
            a = a0;
            b = b0;
        }

        double[] geod = new double[3];

        double xp = x[0] * scale + dx;
        double yp = x[1] * scale + dy;
        double zp = x[2] * scale + dz;

        if (b <= 6000000) {
            b = a - a / b;
        }
        double e2 = (a * a - b * b) / (a * a);
        double s = Math.sqrt(xp * xp + yp * yp);
        geod[1] = Math.atan2(yp, xp);
        if (geod[1] < 0) {
            geod[1] += Math.PI;
        }
        if (yp < 0) {
            geod[1] += 2 * Math.PI;
        }
        double zps = zp / s;
        geod[2] = Math.sqrt(xp * xp + yp * yp + zp * zp) - a;
        geod[0] = Math.atan(zps / (1.0 - e2 * a / (a + geod[2])));

        double n = 1;
        double rhd = 1;
        double rbd = 1;
        while (rbd * n > 1e-4 || rhd > 1e-4) {
            n = a / Math.sqrt(1.0 - e2 * Math.sin(geod[0]) * Math.sin(geod[0]));
            double tmp1 = geod[0];
            double tmp2 = geod[2];
            geod[2] = s / Math.cos(geod[0]) - n;
            geod[0] = Math.atan(zps / (1.0 - e2 * n / (n + geod[2])));
            rbd = Math.abs(tmp1 - geod[0]);
            rhd = Math.abs(tmp2 - geod[2]);
        }
        return geod;
    }

    public static double[] blhxyz(double lat, double lon, double h, double a0, double b0) {
        double a, b;
        if (a0 == 0.0 || b0 == 0.0) {
            a = 6378137.0;
            b = 298.257223563;
        } else {
            a = a0;
            b = b0;
        }
        if (b <= 6000000) {
            b = a - a / b;
        }
        double e2 = (a * a - b * b) / (a * a);

        double W = Math.sqrt(1 - e2 * Math.pow(Math.sin(lat), 2));
        double N = a / W;
        double x = (N + h) * Math.cos(lat) * Math.cos(lon);
        double y = (N + h) * Math.cos(lat) * Math.sin(lon);
        double z = (N * (1 - e2) + h) * Math.sin(lat);

        return new double[]{x, y, z};
    }

    public static double[][] rot_xyz2enu_rad(double lat, double lon) {
        double[][] rotmat = new double[3][3];

        double coslat = Math.cos(lat);
        double sinlat = Math.sin(lat);
        double coslon = Math.cos(lon);
        double sinlon = Math.sin(lon);

        rotmat[0][0] = -sinlon;
        rotmat[0][1] = coslon;
        rotmat[0][2] = 0.0;
        rotmat[1][0] = -sinlat * coslon;
        rotmat[1][1] = -sinlat * sinlon;
        rotmat[1][2] = coslat;
        rotmat[2][0] = coslat * coslon;
        rotmat[2][1] = coslat * sinlon;
        rotmat[2][2] = sinlat;

        return rotmat;
    }

    public static double[][] pos2enu(double[] pos_1, double[] pos_2) {
        double[] geod = xyz2blh(pos_1, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        System.out.printf("%.11f %.11f %.11f \n" , geod[0], geod[1], geod[2]);
        double[][] rot_l2f = rot_xyz2enu_rad(geod[0], geod[1]);
        for (int i = 0; i < rot_l2f.length; i++) {
            for (int j = 0; j < rot_l2f[i].length; j++) {
                System.out.print(rot_l2f[i][j] + " ");
            }
            System.out.println();
        }
        double[][] pos_1_mat = {pos_1};
        double[][] pos_2_mat = {pos_2};

        double[][] pos_1_transpose = transposeMatrix(pos_1_mat);

        double[][] pos_2_transpose = transposeMatrix(pos_2_mat);

        double[][] enu_2 = matrixMultiplication(rot_l2f, matrixSubtraction(pos_2_transpose, pos_1_transpose));
        return enu_2;
    }


    public static double[][] transposeMatrix(double[][] matrix) {
        double[][] result = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    public static double[][] matrixSubtraction(double[][] matrix1, double[][] matrix2) {
        double[][] result = new double[matrix1.length][matrix1[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
        return result;
    }

    public static double[][] matrixMultiplication(double[][] matrix1, double[][] matrix2) {
        int m1Rows = matrix1.length;
        int m1Cols = matrix1[0].length;
        int m2Rows = matrix2.length;
        int m2Cols = matrix2[0].length;

        if (m1Cols != m2Rows) {
            throw new IllegalArgumentException("Matrix dimensions are not compatible for multiplication");
        }

        double[][] result = new double[m1Rows][m2Cols];

        for (int i = 0; i < m1Rows; i++) {
            for (int j = 0; j < m2Cols; j++) {
                for (int k = 0; k < m1Cols; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return result;
    }


    public static void main2() {
        double[] pos_1 = {-2258208.214700, 5020578.919700, 3210256.397500};
        double[] pos_2 = {-2258209.214700, 5020579.919700, 3210258.397500};

        double[][] result = pos2enu(pos_1, pos_2);
        for (double[] row : result) {
            System.out.print("fffff");
            System.out.println(Arrays.toString(row));
        }
    }
}

