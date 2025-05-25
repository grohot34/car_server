-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: car_dealing
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `car_dealing`
--

/*!40000 DROP DATABASE IF EXISTS `car_dealing`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `car_dealing` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `car_dealing`;

--
-- Table structure for table `cars`
--

DROP TABLE IF EXISTS `cars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cars` (
  `id` int NOT NULL AUTO_INCREMENT,
  `vin` varchar(17) NOT NULL,
  `brand` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  `year` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `warranty_years` int DEFAULT '0',
  `available` tinyint(1) DEFAULT '1',
  `quantity` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `vin` (`vin`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cars`
--

LOCK TABLES `cars` WRITE;
/*!40000 ALTER TABLE `cars` DISABLE KEYS */;
INSERT INTO `cars` VALUES (1,'VIN000001','Toyota','Camry',2022,3000000.00,3,0,0),(2,'VIN000002','Hyundai','Solaris',2023,1700000.00,2,1,6),(3,'VIN000003','BMW','X5',2021,6000000.00,4,0,0),(4,'2','gfsd','gsf',5,4375.00,4,1,5);
/*!40000 ALTER TABLE `cars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clients`
--

DROP TABLE IF EXISTS `clients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clients` (
  `id` int NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `address` text,
  PRIMARY KEY (`id`),
  CONSTRAINT `clients_ibfk_1` FOREIGN KEY (`id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clients`
--

LOCK TABLES `clients` WRITE;
/*!40000 ALTER TABLE `clients` DISABLE KEYS */;
INSERT INTO `clients` VALUES (3,'Левонов Алексей Николаевич','+375290000001','liavonau1@example.by','Минск, пр-т Независимости, 10'),(4,'Степанова Ольга Францевна','+375290000002','stsiapanava2@example.by','Гродно, ул. Советская, 15'),(5,'Беляев Дмитрий Олегович','+375290000003','bialiaeu3@example.by','Витебск, ул. Ленина, 5'),(6,'Ковальчук Наталья Альбертовна','+375290000004','kavalchuk4@example.by','Могилёв, ул. Первомайская, 20'),(7,'Карпович Евгений Александрович','+375290000005','krapovich5@example.by','Гомель, ул. Советская, 33'),(8,'Романовская Светлана Никитична','+375290000006','ramanouskaya6@example.by','Брест, ул. Московская, 7');
/*!40000 ALTER TABLE `clients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders_to_sales`
--

DROP TABLE IF EXISTS `orders_to_sales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders_to_sales` (
  `id` int NOT NULL AUTO_INCREMENT,
  `client_id` int NOT NULL,
  `car_id` int NOT NULL,
  `order_date` date NOT NULL,
  `status` varchar(50) DEFAULT 'Ожидает подтверждения',
  `payment_method` varchar(50) DEFAULT NULL,
  `total_price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `client_id` (`client_id`),
  KEY `car_id` (`car_id`),
  CONSTRAINT `orders_to_sales_ibfk_1` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`),
  CONSTRAINT `orders_to_sales_ibfk_2` FOREIGN KEY (`car_id`) REFERENCES `cars` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders_to_sales`
--

LOCK TABLES `orders_to_sales` WRITE;
/*!40000 ALTER TABLE `orders_to_sales` DISABLE KEYS */;
INSERT INTO `orders_to_sales` VALUES (1,5,1,'2025-05-01','Отменён',NULL,NULL),(2,5,3,'2025-05-01','Отменён',NULL,NULL),(3,5,3,'2025-05-01','Отменён','Кредит',6000000.00),(4,5,2,'2025-05-01','Подтверждён','Наличные',1700000.00),(5,5,3,'2025-05-01','Отменён','Наличные',6000000.00),(6,5,1,'2025-05-01','Подтверждён','Наличные',3000000.00),(7,5,2,'2025-05-03','Подтверждён','Наличные',1700000.00),(8,5,1,'2025-05-03','Подтверждён','Карта',3000000.00),(9,5,3,'2025-05-04','Подтверждён','Другое',6000000.00),(10,5,3,'2025-05-04','Подтверждён','Карта',6000000.00),(11,5,3,'2025-05-04','Подтверждён','Кредит',6000000.00),(12,5,3,'2025-05-04','Подтверждён','Наличные',6000000.00),(13,5,3,'2025-05-04','Подтверждён','Карта',6000000.00),(14,5,3,'2025-05-04','Подтверждён','Наличные',6000000.00),(15,5,3,'2025-05-04','Отменён','Наличные',6000000.00),(16,5,3,'2025-05-04','Отменён','Карта',6000000.00),(17,5,3,'2025-05-04','Подтверждён','Наличные',6000000.00),(18,5,3,'2025-05-04','Подтверждён','Кредит',6000000.00),(19,5,3,'2025-05-04','Подтверждён','Наличные',6000000.00),(20,5,3,'2025-05-04','Отменён','Карта',6000000.00),(21,5,3,'2025-05-04','Отменён','Наличные',6000000.00),(22,5,3,'2025-05-04','Отменён','Карта',6000000.00),(23,5,3,'2025-05-04','Ожидает подтверждения','Наличные',6000000.00),(24,5,3,'2025-05-04','Ожидает подтверждения','Карта',6000000.00),(25,5,3,'2025-05-04','Ожидает подтверждения','Карта',6000000.00),(33,5,3,'2025-05-05','Отменён','Наличные',6000000.00),(34,5,3,'2025-05-05','Подтверждён','Наличные',6000000.00),(35,5,1,'2025-05-09','Подтверждён','Наличные',3000000.00),(36,5,1,'2025-05-09','Подтверждён','Другое',3000000.00),(37,5,1,'2025-05-09','Подтверждён','Наличные',3000000.00),(38,5,1,'2025-05-09','Подтверждён','Наличные',3000000.00),(39,5,1,'2025-05-09','Ожидает подтверждения','Наличные',3000000.00),(40,5,1,'2025-05-09','Ожидает подтверждения','Наличные',3000000.00),(41,5,2,'2025-05-09','Подтверждён','Наличные',1700000.00);
/*!40000 ALTER TABLE `orders_to_sales` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sales`
--

DROP TABLE IF EXISTS `sales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales` (
  `id` int NOT NULL AUTO_INCREMENT,
  `car_id` int NOT NULL,
  `client_id` int NOT NULL,
  `sale_date` date NOT NULL,
  `sale_price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `car_id` (`car_id`),
  KEY `client_id` (`client_id`),
  CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`car_id`) REFERENCES `cars` (`id`) ON DELETE CASCADE,
  CONSTRAINT `sales_ibfk_2` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales`
--

LOCK TABLES `sales` WRITE;
/*!40000 ALTER TABLE `sales` DISABLE KEYS */;
INSERT INTO `sales` VALUES (1,1,3,'2024-04-01',2950000.00),(2,2,4,'2024-04-02',1650000.00),(3,3,5,'2024-04-03',5800000.00),(4,1,5,'2025-05-01',3000000.00),(5,2,5,'2025-05-03',1700000.00),(6,3,5,'2025-05-03',6000000.00),(7,1,5,'2025-05-03',3000000.00),(8,3,5,'2025-05-04',6000000.00),(9,3,5,'2025-05-04',6000000.00),(10,3,5,'2025-05-04',6000000.00),(11,3,5,'2025-05-04',6000000.00),(12,3,5,'2025-05-04',6000000.00),(13,3,5,'2025-05-04',6000000.00),(14,3,5,'2025-05-04',6000000.00),(15,3,5,'2025-05-09',6000000.00),(16,1,5,'2025-05-09',3000000.00),(17,1,5,'2025-05-09',3000000.00),(18,1,5,'2025-05-09',3000000.00),(19,1,5,'2025-05-09',3000000.00),(20,2,5,'2025-05-09',1700000.00);
/*!40000 ALTER TABLE `sales` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_records`
--

DROP TABLE IF EXISTS `service_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_records` (
  `id` int NOT NULL AUTO_INCREMENT,
  `car_id` int NOT NULL,
  `client_id` int NOT NULL,
  `service_date` date NOT NULL,
  `description` text NOT NULL,
  `under_warranty` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `car_id` (`car_id`),
  KEY `client_id` (`client_id`),
  CONSTRAINT `service_records_ibfk_1` FOREIGN KEY (`car_id`) REFERENCES `cars` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_records_ibfk_2` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_records`
--

LOCK TABLES `service_records` WRITE;
/*!40000 ALTER TABLE `service_records` DISABLE KEYS */;
INSERT INTO `service_records` VALUES (1,1,3,'2025-04-01','Плановое ТО',1),(2,2,4,'2025-04-02','Замена масла',1),(3,3,5,'2025-04-03','Замена тормозных колодок',1);
/*!40000 ALTER TABLE `service_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `login` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('ADMIN','CLIENT','MANAGER') NOT NULL,
  `is_blocked` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `login` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','ADMIN',0),(2,'distill','8fc7c369e975ce040f8e211015c641b6e4087e1bdc93f87df00476b4e35403a9','MANAGER',0),(3,'frog','74fa5327cc0f4e947789dd5e989a61a8242986a596f170640ac90337b1da1ee4','CLIENT',0),(4,'lion','fc59487712bbe89b488847b77b5744fb6b815b8fc65ef2ab18149958edb61464','CLIENT',0),(5,'cat','77af778b51abd4a3c51c5ddd97204a9c3ae614ebccb75a606c3b6865aed6744e','CLIENT',0),(6,'patsapina','4511de4f552ac9410c505702f7fcd769439237eafeba3e30a47a76bb23bed508','CLIENT',0),(7,'dog','cd6357efdd966de8c0cb2f876cc89ec74ce35f0968e11743987084bd42fb8944','CLIENT',0),(8,'mouse','47c5c28cae2574cdf5a194fe7717de68f8276f4bf83e653830925056aeb32a48','CLIENT',0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warranty`
--

DROP TABLE IF EXISTS `warranty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warranty` (
  `id` int NOT NULL AUTO_INCREMENT,
  `car_id` int NOT NULL,
  `warranty_start_date` date NOT NULL,
  `warranty_end_date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `car_id` (`car_id`),
  CONSTRAINT `warranty_ibfk_1` FOREIGN KEY (`car_id`) REFERENCES `cars` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warranty`
--

LOCK TABLES `warranty` WRITE;
/*!40000 ALTER TABLE `warranty` DISABLE KEYS */;
INSERT INTO `warranty` VALUES (1,1,'2024-04-01','2027-04-01'),(2,2,'2024-04-02','2026-04-02'),(3,3,'2024-04-03','2028-04-03');
/*!40000 ALTER TABLE `warranty` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-10 20:31:36
