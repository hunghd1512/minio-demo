***

### 1. **Versioning**

- **Ý nghĩa:** Lưu nhiều phiên bản của một object, giúp khôi phục khi xóa/ghi nhầm hoặc rollback.
- **Cách bật:**
    - Dùng MinIO Client (CLI):

```shell
mc version enable <alias>/<bucket>
```

    - Khi upload object trùng tên trong bucket có versioning, Minio sẽ lưu bản mới, bản cũ thành phiên bản trước.

***

### 2. **Tag**

- **Ý nghĩa:** Gán các cặp key-value cho object để quản lý, filter/phân loại.
- **Cách sử dụng:**
    - Qua API Java:

```java
minioClient.setObjectTags(
    SetObjectTagsArgs.builder()
    .bucket("my-bucket")
    .object("data.txt")
    .tags(new Tags().put("project", "bigdata"))
    .build()
);
```

    - Hoặc CLI:

```shell
mc tag set <alias>/<bucket>/<object> project=bigdata env=prod
```


***

### 3. **Lifecycle**

- **Ý nghĩa:** Tự động xóa, chuyển vùng lưu trữ, hoặc xử lý file cũ (ví dụ xóa object >30 ngày).
- **Cách khai báo:**
    - Tạo file XML/yaml policy (giống S3), nạp bằng CLI admin hoặc config trực tiếp.

```shell
mc ilm import <alias>/<bucket> lifecycle.json
```

    - Policy ví dụ:

```json
{
  "Rules": [
    {
      "ID": "DeleteOldFiles",
      "Status": "Enabled",
      "Expiration": {"Days":30}
    }
  ]
}
```


***

### 4. **Permission**

- **Cơ chế:** MinIO hỗ trợ `Policy-based access control` giống AWS IAM (buckets, prefix, actions).
- **Các bước điển hình:**

1. Tạo user mới:

```shell
mc admin user add <alias> newuser newpass
```

2. Gán policy read-only:

```shell
mc admin policy set <alias> readwrite user=newuser
```

3. Tạo custom policy:

```shell
mc admin policy add <alias> mypolicy mypolicy.json
mc admin policy set <alias> mypolicy user=newuser
```


***

### 5. **Multipart (Phân mảnh) cho file >5GB**

- **Ý nghĩa:** File lớn sẽ tự động cấu thành nhiều part nhỏ, upload song song, tăng tốc và hồi phục dễ dàng hơn nếu lỗi mạng.
- **Thực hiện (Java MinIO):**
    - SDK tự động xử lý phân mảnh nếu bạn upload file lớn thông qua `putObject` (không cần manual multipart như AWS SDK).
    - Nếu upload file cực lớn, cấu hình thêm `partSize` nếu cần siêu tối ưu.

***

### 6. **Di chuyển dữ liệu MinIO sang S3**

- **Cách 1 (CLI):**
  Sử dụng `mc mirror`:

```shell
mc mirror minio/sourcebucket s3/targetbucket
```

Đẩy đồng bộ object từ Minio qua S3, bảo toàn metadata/tags/version.
- **Cách 2 (code Java):**
    - Đọc object từ MinIO qua inputstream, ghi sang S3 bằng S3 SDK.
    - Dùng vòng lặp để migrate nhiều file (nếu nhiều object lớn).

***

### 7. **Buckets: Public vs. Private**

|  | Public Bucket | Private Bucket |
| :-- | :-- | :-- |
| **Truy cập** | Ai biết endpoint, bucket, object name đều tải file (không cần auth) | Phải có access/secret key hoặc presigned URL |
| **Ứng dụng** | Lưu ảnh banner/load public asset, file không nhạy cảm | Lưu file nhạy cảm, user upload/download приват |
| **Presigned URL** | Không cần (chỉ link trực tiếp) | Bắt buộc dùng presigned url nếu không key |
| **Rủi ro** | Lỡ public file nhạy cảm, search engine crawl | Sử dụng đúng policy, kiểm soát truy cập |

- **Bản chất:** Public bucket là ai cũng lấy được; private bucket cần xác thực hoặc generate URL (có expired).

***

### 8. **User/Access Key Default và Thực hành An Toàn**

- **Mặc định:** MinIO khi cài mới sẽ có `MINIO_ROOT_USER` (access key) và `MINIO_ROOT_PASSWORD` (secret key), thường là "minioadmin" (nên đổi ngay khi deploy).
- **Khuyến cáo:**
    - Đăng nhập Minio Console, tạo user/key mới, không dùng cặp mặc định cho app.
    - Xoá hoặc disable tài khoản gốc khỏi production hoàn toàn.
    - Document kỹ cách thêm user/key mới (CLI hoặc Console UI) vào **README**:

1. Đăng nhập Minio Console (bằng admin user).
2. Vào mục **Access Management > Users**.
3. Tạo user mới lưu lại Access Key \& Secret Key.
4. Gán policy phù hợp (read, write, admin hoặc custom).
5. Update app config để dùng cặp access key/secret key mới.
6. Xóa/tắt user mặc định (optional, nếu không còn cần nữa).

***

### 9. **Thông Tin Bổ Sung cho README**

Thêm đoạn hướng dẫn vào README, ví dụ:

```
❗ **Không sử dụng tài khoản admin mặc định trong production.**
1. Đăng nhập MinIO Console bằng admin ban đầu.
2. Truy cập Access Management → Users → Create User.
3. Nhập user name, sinh cặp Access Key / Secret Key, lưu trữ cẩn thận.
4. Gán policy tùy thuộc vai trò sử dụng (chỉ đọc, chỉ ghi, full access).
5. Cập nhật ứng dụng của bạn dùng cặp key mới.
6. Xóa/disable user mặc định khi đã thay thế xong.
```


***

Nếu cần ví dụ code mẫu từng phần (Java code lifecycle, tag, policy, presigned URL...), hãy hỏi cụ thể!

