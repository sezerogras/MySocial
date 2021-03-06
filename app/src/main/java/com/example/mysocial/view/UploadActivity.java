package com.example.mysocial.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.mysocial.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    Uri imageData;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    //Bitmap selectedImage;

    private ActivityUploadBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();
        firebaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();



    }

    public  void uploadButtonClicked(View view ){ // buton metodumu buraya ba??lad??m

        // artik postumuzu sunucuya yukleme vakti geldi

        if(imageData != null){
            // universal uniquie id yani her seferinde farkl?? bir id ismi olu??turuyor yapma sebebim veri taban??na her yeni resim ekledi??imde ayn?? isimle kaydoluyor ve b??r ??nceki siliniyor bu yuzden bu i??lemi yapt??m
            UUID uuid = UUID.randomUUID(); // yani rastgele bir isim olu??tur
            String imageName = "/Images" + uuid + ".jpg";

            // burada art??k referans de??ere ula??ma i??lemi yap??yorum
            // referans storage k??sm??na nerede ne kaydedece??imi tutan bir obje

            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // ba??ar??l???? olursa ne yapaca????m Download url yi alaca????m

                    StorageReference newReference = firebaseStorage.getReference(imageName); // imagenin referans??na ula??maya ??al??????yorum
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString(); // gorsel burada
                            String comment = binding.commentText.getText().toString(); // kullan??c??n??n yorumunu ald??m , yorum  burada
                            FirebaseUser user = auth.getCurrentUser(); // kimin papyla??t??????n?? g??remk i??in kullaln??c??y?? ald??m
                            String email = user.getEmail(); // kullan??c??n??n emailini de bu ??ekilde g??rm???? oldum


                            // veri taban??na  anahtar kelime ve de??er e??le??mesi ger??ekle??iyor bu da bana neyi hat??rlatt?? tabi ki hash map !
                            HashMap<String , Object>postData = new HashMap<>(); // burada anahtar String tipinde oldu deger yani value object tipinde yazmama??n sebebi herhangi bir veri turu geliyor sonucta int string tarih vs herhangi bir sey olabilir
                            postData.put("useremail",email);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp()); // burada tarihi ayr?? yazmad??m cunku fireBase de otomatikmen uyumlun olarak guncel tarih geliyor


                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    // ve bu kadar ert??k veri taban??ma i??lemlerimi ekleyebilirim


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show(); // e??er bir hata olursa gene bana hata mesaj?? yazd??rs??n



                                }
                            });




                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show(); // daha ??nce yapt????????m??n ayn??s??



                }
            });
        }


    }

    public void selectImage(View view ){   // select e t??kald??????mda ne olac??????n?? yapmas?? i??in metodumu buraya tan??mlad??m

        // izin i??lemleri ger??ekle??ti


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { // e??er izin yoksa ne yapaca????m

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) { // e??er iziin varsa izini g??stermemmi istiyor

                Snackbar.make(view, "Permission needed for gallery!", Snackbar.LENGTH_INDEFINITE).setAction("Give permission!", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // burada izini istedim (ask permission )
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show();


            } else {
                // gene izin isteyece??im sadece di??erinde kullan??c??ya extra yapmas?? gerkenleri soyluyorum
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        } else {

            // izin zaten verilmi?? o yuzden galeriye gitmesini sa??layaca????m
            Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intenttoGallery);



        }

    }

    private void registerLauncher(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if(intentFromResult != null){
                        // bu i??lemde bana bir uri d??nd??recek , uri bana verinin nerede kay??tl?? oldu??unu s??yleyecek
                      imageData=intentFromResult.getData();
                      binding.imageView.setImageURI(imageData); // imageyi g??stermek i??in birinci y??ntem
 /*

 // ikinci i??lem
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(UploadActivity.this.getContentResolver(),imageData);
                                 selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            }
                            else {
                                selectedImage = MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);

                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

  */
                    }


                }

            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if (result){
                    Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intenttoGallery);

                }else {
                    Toast.makeText(UploadActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}