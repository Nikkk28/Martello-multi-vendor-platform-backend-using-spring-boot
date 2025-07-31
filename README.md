# Martello E-commerce Platform

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

A comprehensive multi-vendor e-commerce platform built with **Spring Boot** that enables seamless online marketplace operations with robust vendor management, secure authentication, and advanced e-commerce features.

## Features

### **Authentication & Authorization**
- JWT-based secure authentication
- Role-based access control (Customer, Vendor, Admin)
- Refresh token mechanism
- Password encryption with BCrypt

### **Multi-Vendor Support**
- Vendor registration and approval workflow
- Vendor dashboard with analytics
- Commission management system
- Vendor-specific product management

### 🛒 **Shopping Experience**
- Advanced product catalog with categories
- Product variations and inventory management
- Shopping cart with multi-vendor support
- Wishlist functionality
- Product reviews and ratings

### **Order Management**
- Comprehensive order processing workflow
- Order status tracking
- Vendor-specific order management
- Order history and analytics

### **Financial Features**
- Dynamic commission rate system
- Discount and coupon management
- Revenue tracking and reporting
- Vendor earnings calculation

### **Communication**
- Real-time notification system
- Admin and vendor notifications
- Order status updates
- Review approval notifications

### **Analytics & Reporting**
- Admin dashboard with key metrics
- Vendor performance analytics
- Sales trend analysis
- Product performance tracking

### **File Management**
- Secure file upload system
- Image storage for products
- File validation and size limits

## Architecture

### **Core Services**

| Service | Description |
|---------|-------------|
| `AuthService` | Handles user authentication, registration, and token management |
| `UserService` | Manages user profiles and Spring Security integration |
| `VendorService` | Vendor approval workflow and dashboard analytics |
| `ProductService` | Product CRUD operations and catalog management |
| `CategoryService` | Product category management |
| `CartService` | Shopping cart operations with multi-vendor support |
| `OrderService` | Order processing and management |
| `ReviewService` | Product review and rating system |
| `WishlistService` | User wishlist management |
| `DiscountService` | Coupon and discount management |
| `CommissionService` | Vendor commission calculation and payment |
| `NotificationService` | Real-time notification system |
| `FileStorageService` | File upload and storage management |
| `AdminService` | Administrative dashboard and metrics |

### **Key Technologies**
- **Backend**: Spring Boot, Spring Security, Spring Data JPA
- **Database**: JPA/Hibernate with relational database
- **Authentication**: JWT tokens with refresh token mechanism
- **File Storage**: Local file system with validation
- **Notifications**: Event-driven notification system

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Database (MySQL/PostgreSQL recommended)
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/martello-ecommerce.git
   cd martello-ecommerce
   ```

2. **Configure database connection**
   ```properties
   # application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/martello_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

3. **Set up file upload directory**
   ```properties
   file.upload-dir=uploads
   ```

4. **Configure JWT settings**
   ```properties
   jwt.secret=your-secret-key
   jwt.expiration=86400000
   ```

5. **Build and run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

6. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - Upload Directory: `http://localhost:8080/uploads`

## API Documentation

### **Authentication Endpoints**
```http
POST /api/auth/register     # User registration
POST /api/auth/login        # User login
POST /api/auth/refresh      # Refresh access token
POST /api/auth/logout       # User logout
```

### **Product Management**
```http
GET    /api/products              # Get all products
GET    /api/products/{id}         # Get product by ID
POST   /api/products              # Create product (Vendor)
PUT    /api/products/{id}         # Update product (Vendor)
DELETE /api/products/{id}         # Delete product (Vendor)
GET    /api/products/featured     # Get featured products
```

### **Cart Operations**
```http
GET    /api/cart                  # Get user cart
POST   /api/cart/items            # Add item to cart
PUT    /api/cart/items/{id}       # Update cart item
DELETE /api/cart/items/{id}       # Remove cart item
DELETE /api/cart                  # Clear cart
```

### **Order Management**
```http
GET    /api/orders                # Get user orders
POST   /api/orders                # Create order
PUT    /api/orders/{id}/status    # Update order status (Vendor)
GET    /api/orders/vendor         # Get vendor orders
```

### **Vendor Operations**
```http
GET    /api/vendor/dashboard      # Vendor dashboard
GET    /api/vendor/products       # Vendor products
POST   /api/admin/vendors/{id}/approve  # Approve vendor (Admin)
```

## 🔧 Configuration

### **Application Properties**
```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/martello_db
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

# File Upload Configuration
file.upload-dir=uploads
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB

# JWT Configuration
jwt.secret=mySecretKey
jwt.expiration=86400000

# Logging Configuration
logging.level.com.martello.ecommerce=DEBUG
```

## Database Schema

### **Core Entities**
- **User**: Customer, Vendor, and Admin user accounts
- **VendorProfile**: Vendor-specific information and approval status
- **Product**: Product catalog with categories and variations
- **Category**: Product categorization system
- **Cart/CartItem**: Shopping cart functionality
- **Order/OrderItem**: Order processing and tracking
- **Review**: Product reviews and ratings
- **Wishlist**: User wishlist management
- **Discount**: Coupon and discount system
- **Commission**: Vendor commission tracking
- **Notification**: Real-time notification system

### **Entity Relationships**
```
User (1) ←→ (1) VendorProfile
User (1) ←→ (*) Order
User (1) ←→ (*) Review
User (1) ←→ (*) Wishlist
Product (*) ←→ (1) Category
Product (*) ←→ (1) VendorProfile
Order (1) ←→ (*) OrderItem
Cart (1) ←→ (*) CartItem
```

## Security Features

### **Authentication Flow**
1. User registers/logs in with credentials
2. JWT access token and refresh token issued
3. Access token used for API authentication
4. Refresh token used to renew expired access tokens
5. Role-based authorization for different endpoints

### **Authorization Levels**
- **Public**: Product browsing, registration
- **Customer**: Cart, orders, reviews, wishlist
- **Vendor**: Product management, order fulfillment
- **Admin**: User management, vendor approval, system analytics

### **Data Protection**
- Password encryption using BCrypt
- JWT token validation
- File upload security with type and size validation
- SQL injection prevention with JPA

## Business Logic

### **Vendor Approval Workflow**
1. Vendor registers with business information
2. Admin receives notification of new vendor
3. Admin reviews and approves/rejects vendor
4. Approved vendors can add and manage products
5. Commission rates are configured per vendor/category

### **Order Processing**
1. Customer adds products to cart (grouped by vendor)
2. Order created per vendor with commission calculation
3. Inventory automatically updated
4. Vendor receives order notification
5. Vendor updates order status throughout fulfillment

### **Commission System**
- Dynamic commission rates per vendor/category
- Automatic commission calculation on order creation
- Vendor earnings tracking and payment processing
- Commission rate management by administrators

## Testing

### **Running Tests**
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Run with coverage
mvn test jacoco:report
```

### **Test Categories**
- **Unit Tests**: Service layer testing with mocked dependencies
- **Integration Tests**: Repository layer and database testing
- **Security Tests**: Authentication and authorization testing

## Deployment

### **Production Deployment**
1. **Environment Configuration**
   ```properties
   spring.profiles.active=production
   spring.datasource.url=${DATABASE_URL}
   jwt.secret=${JWT_SECRET}
   ```

2. **Docker Deployment**
   ```dockerfile
   FROM openjdk:17-jre-slim
   COPY target/martello-ecommerce.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```

3. **Database Migration**
   ```bash
   # Use Flyway or Liquibase for production migrations
   mvn flyway:migrate
   ```

##  Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### **Code Style**
- Follow Java coding conventions
- Use meaningful variable and method names
- Add comprehensive Javadoc comments
- Maintain test coverage above 80%

##  TODO / Roadmap

- [ ] **Payment Integration**: Stripe/PayPal integration
- [ ] **Email Service**: Order confirmations and notifications
- [ ] **Search & Filtering**: Elasticsearch integration
- [ ] **Caching**: Redis caching for improved performance
- [ ] **API Documentation**: Swagger/OpenAPI integration
- [ ] **Mobile API**: React Native or Flutter support
- [ ] **Analytics**: Advanced reporting and analytics
- [ ] **Internationalization**: Multi-language support
      
## 👨‍💻 Author

**Martello Team**
- Email: cmarthanikshith.28@gmail.com

---

**⭐ Star this repository if you find it helpful!**

For support, please contact us at [marthanikshith.28@gmail.com](mailto:marthanikshith.28@gmail.com)
