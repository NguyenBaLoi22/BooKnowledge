package com.example.booknowledge_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booknowledge_app.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class PdfViewActivity extends AppCompatActivity {
    private static final String TAG = "PDF_VIEW_TAG";
    Context context;
    private ActivityPdfViewBinding binding;
    String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();

        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "onCreate: bookID : " + bookId);
        loadBookDetails();


        binding.jumpNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pdfView.jumpTo(binding.pdfView.getCurrentPage() + 1);

            }
        });
        binding.jumpPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pdfView.jumpTo(binding.pdfView.getCurrentPage() - 1);

            }
        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        binding.SHOWALL.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Hàm này được gọi khi người dùng nhấn giữ lâu trên màn hình
                // Ẩn điều khiển trên màn hình nếu chúng đang hiển thị
                if (binding.toolbarRL.getVisibility() == View.VISIBLE) {
                    hideAllControl();
                }
                // Hiển thị điều khiển trên màn hình nếu chúng đang bị ẩn
                else {
                    showAllControl();
                }
                return true;
            }
        });


    }


    private void showAllControl() {
        binding.toolbarRL.setVisibility(View.VISIBLE);
        binding.jumpNextBtn.setVisibility(View.VISIBLE);
        binding.jumpPreBtn.setVisibility(View.VISIBLE);

    }

    private void hideAllControl() {
        binding.toolbarRL.setVisibility(View.INVISIBLE);
        binding.jumpNextBtn.setVisibility(View.INVISIBLE);
        binding.jumpPreBtn.setVisibility(View.INVISIBLE);

    }

    private void loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Lấy Pdf URL từ db...");
        //lấy url sách sử dụng bookId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //lấy url sách
                        String pdfUrl = "" + snapshot.child("url").getValue();
                        Log.d(TAG, "onDataChange: Pdf URL: " + pdfUrl);
                        //Load pdf bằng url đã nhận từ firebase storage
                        loadBookFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: Lấy Pdf từ storage...");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        binding.pdfView.fromBytes(bytes)
                                .defaultPage(0)
                                .enableSwipe(true)
                                .spacing(10)
                                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                                .swipeHorizontal(false) //false để cuộn theo chiều dọc, true để lướt sang ngang
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        //set trang hiện tại và tổng số trang lên subtitle
                                        int currentPage = (page + 1); // +1 vì page bắt đầu từ 0
                                        binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount);
                                        Log.d(TAG, "onPageChanged: " + currentPage + "/" + pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d(TAG, "onError: " + t.getMessage());
                                        Toast.makeText(PdfViewActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                        Toast.makeText(PdfViewActivity.this, "Lỗi trang " + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .load();
                        binding.progressBar.setVisibility(View.GONE);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        binding.progressBar.setVisibility(View.GONE);

                    }
                });
    }


}
