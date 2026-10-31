# ==========================================
# Giai đoạn 1: Build source code với Maven
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy wrapper và file cấu hình Maven để tận dụng Docker layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Cấp quyền thực thi cho Maven Wrapper và tải trước các dependency
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy toàn bộ mã nguồn vào container
COPY src ./src

# Build ứng dụng và bỏ qua test để tăng tốc độ đóng gói image
RUN ./mvnw clean package -DskipTests

# ==========================================
# Giai đoạn 2: Runtime image gọn nhẹ và bảo mật
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Tạo group và user không có quyền root để đảm bảo an toàn bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy file jar đã build từ builder stage
COPY --from=builder /app/target/*.jar app.jar

# Gán quyền sở hữu file jar cho non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Cổng mặc định của ứng dụng Spring Boot
EXPOSE 8080

# Cấu hình biến môi trường JVM tối ưu cho container trên AWS
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]