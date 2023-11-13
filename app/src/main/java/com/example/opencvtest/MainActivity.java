package com.example.opencvtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "TEST_OPEN_CV_ANDROID";
    private static final int PICK_IMAGE_REQUEST_1 = 1;
    private static final int PICK_IMAGE_REQUEST_2 = 2;

    private ImageButton image1, image2;
    private Button testButton;
    private Uri imageUri1, imageUri2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV 초기화 실패!");
        } else {
            Log.d(TAG, "OpenCV 초기화 성공!!!!!");
        }

        image1 = (ImageButton) findViewById(R.id.image1);
        image2 = (ImageButton) findViewById(R.id.image2);
        testButton = (Button) findViewById(R.id.testButton);

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이미지 유사도 검사 로직
                if (imageUri1 != null && imageUri2 != null) {
                    try {
                        // Convert URI to OpenCV Mat
                        Mat img1 = uriToMat(imageUri1);
                        Mat img2 = uriToMat(imageUri2);

                        boolean areSimilar = compareImages(img1, img2);

                        if (areSimilar) {
                            // 두 개의 이미지가 유사할 경우
                            Toast.makeText(MainActivity.this, "유사합니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            // 두 개의 이미지가 유사하지 않을 경우
                            Toast.makeText(MainActivity.this, "유사하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "이미지를 첨부해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이미지 선택 인텐트를 생성
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_1);
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이미지 선택 인텐트를 생성
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_2);
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_1 && resultCode == RESULT_OK) {
            imageUri1 = data.getData();
            image1.setImageURI(imageUri1);
            Log.d("MainActivity", PICK_IMAGE_REQUEST_1+"image1에 들어갔음");
        } else if (requestCode == PICK_IMAGE_REQUEST_2 && resultCode == RESULT_OK) {
            imageUri2 = data.getData();
            image2.setImageURI(imageUri2);
            Log.d("MainActivity", PICK_IMAGE_REQUEST_2+"image2에 들어갔음");
        }

    }

    private Mat uriToMat(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        return mat;
    }

    private boolean compareImages(Mat img1, Mat img2) {
        // Add your image comparison logic here using OpenCV functions
        // For example, you can use feature matching algorithms like AKAZE, KAZE, ORB, etc.

        // Sample code using AKAZE feature detector and descriptor matcher
        AKAZE akaze = AKAZE.create();
        MatOfKeyPoint keyPoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();
        akaze.detectAndCompute(img1, new Mat(), keyPoints1, descriptors1);
        akaze.detectAndCompute(img2, new Mat(), keyPoints2, descriptors2);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors1, descriptors2, matches);

        // Filter matches based on a distance threshold
        float maxDist = 40.0f; // You can adjust this threshold based on your needs
        List<DMatch> goodMatches = new ArrayList<>();
        for (DMatch match : matches.toList()) {
            if (match.distance < maxDist) {
                goodMatches.add(match);
            }
        }

        // Display the matched keypoints (optional, for visualization purposes)
        Mat imgMatches = new Mat();
        Features2d.drawMatches(img1, keyPoints1, img2, keyPoints2, new MatOfDMatch(goodMatches.toArray(new DMatch[0])), imgMatches, new Scalar(0, 255, 0), new Scalar(0, 0, 255), new MatOfByte(), Features2d.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS);

        // You can customize the visualization based on your needs
        // For example, you can show imgMatches in an ImageView

        // Return true if the images are considered similar based on your comparison logic
        return goodMatches.size() > 0;
    }


}