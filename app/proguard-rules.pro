# 1. Giữ lại các thuộc tính Generic và Annotation cần thiết cho Firebase/Room
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# 2. Giữ lại các class của Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# 3. Giữ nguyên các class Model (Quan trọng nhất)
# Điều này ngăn R8 đổi tên các trường khiến Firestore không thể đọc/ghi dữ liệu
-keep class com.example.SkyNote.data.model.** { *; }

# 4. Giữ lại các class và members phục vụ cho Reflection (Firestore/Room)
-keepclassmembers class com.example.SkyNote.data.model.** {
    <fields>;
    <init>();
}

# 5. Các quy tắc bổ sung cho Firebase và Google Play Services
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
