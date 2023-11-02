-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 02, 2023 at 06:23 PM
-- Server version: 10.4.13-MariaDB
-- PHP Version: 7.2.31

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ladetech_pos_db`
--
CREATE DATABASE IF NOT EXISTS `ladetech_pos_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ladetech_pos_db`;

-- --------------------------------------------------------

--
-- Table structure for table `company_data`
--

CREATE TABLE `company_data` (
  `companyIdentifier` varchar(10) NOT NULL,
  `companyVatNo` varchar(20) NOT NULL,
  `tel` varchar(20) NOT NULL,
  `email` varchar(50) NOT NULL,
  `poBox` varchar(50) NOT NULL,
  `address` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `bank` varchar(20) NOT NULL,
  `accountNo` varchar(30) NOT NULL,
  `branch` varchar(50) NOT NULL,
  `countryTax` float NOT NULL,
  `invoiceNo` int(5) NOT NULL,
  `quotationNo` int(5) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `company_data`
--

INSERT INTO `company_data` (`companyIdentifier`, `companyVatNo`, `tel`, `email`, `poBox`, `address`, `name`, `bank`, `accountNo`, `branch`, `countryTax`, `invoiceNo`, `quotationNo`) VALUES
('company579', 'C08427001112', '+267 71642764', 'ladetechholdings@gmail.com', '53171 Gaborone', 'Plot 20765 Block 3 Industrial', 'Ladetech Holdings (Pty) Ltd', 'FNB Bank', '62079862051', 'First Place Branch', 0.14, 5, 3);

-- --------------------------------------------------------

--
-- Table structure for table `inventory_data`
--

CREATE TABLE `inventory_data` (
  `product_id` int(11) NOT NULL,
  `product_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stock_level` int(11) NOT NULL,
  `price` float NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `inventory_data`
--

INSERT INTO `inventory_data` (`product_id`, `product_name`, `stock_level`, `price`, `description`) VALUES
(101, 'Cutting UV Gloss (2700 x 1800)', 99999993, 70, 'Cutting charge for UV Gloss\n(2700 x 1800)'),
(102, 'Cutting White Board (2700 x 1800)', 99999995, 50, 'Cutting charge for White Board \n(2730 x 1830)'),
(103, 'UV Gloss (2700 x 1800) (2700mm x 1800mm)', 99999993, 2300, 'UV Gloss (2700 x 1800) (2700mm x \n1800mm)'),
(104, 'White Board (2730mm x 1830mm)', 999995, 590, 'Cost of White Board (2730mm x \n1830mm)'),
(105, 'Edging 2mm x 22mm', 9999843, 12, 'Cost of Buying Edging 2mm x 22mm'),
(106, 'Edging 1mm x 22mm', 99999977, 8.5, 'Cost of Buying Edging 1mm x 22mm'),
(107, 'Edging 0.4mm x 22mm', 999999999, 7, 'Cost of Buying Edging 0.4mm x 22mm'),
(108, 'Sink Cut Out', 99999999, 950, 'Sink Cut Out'),
(109, 'Granite Stove Cut Out ', 9999999, 950, 'Granite Stove Cut Out '),
(110, 'Less Handle Push Latch', 50, 45, 'Less Handle Push Latch'),
(111, 'Labour on Push Latch', 99999, 250, 'Labour on Push Latch'),
(113, 'coloured board', 20, 945, 'coloured board'),
(114, 'Fake product', 0, 100, '');

-- --------------------------------------------------------

--
-- Table structure for table `pos_system`
--

CREATE TABLE `pos_system` (
  `pos_id` varchar(7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `pos_system`
--

INSERT INTO `pos_system` (`pos_id`) VALUES
('pos2023');

-- --------------------------------------------------------

--
-- Table structure for table `tool_data`
--

CREATE TABLE `tool_data` (
  `tool_id` int(11) NOT NULL,
  `tool_name` varchar(255) NOT NULL,
  `quantity` int(11) NOT NULL,
  `possession_of` varchar(100) NOT NULL,
  `details` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `tool_data`
--

INSERT INTO `tool_data` (`tool_id`, `tool_name`, `quantity`, `possession_of`, `details`) VALUES
(1, 'Hammer', 2, 'Stan', 'On site at Maun.'),
(2, 'Dummy tool 1', 69, 'Brian and William.', 'On site in block 10.'),
(3, 'Dummy Tool 2', 12, 'Brian', 'Location on Maun Site.'),
(4, 'Dummy Tool 3', 6, 'Stan', 'On site in Jwaneng.'),
(5, 'Dummy tool 5', 25, 'MEEE', 'All done here!!!!!'),
(6, 'Dummy Tool 6', 66, 'Nobody', 'Gaborone'),
(7, 'error testing ', 12, 'man 1', 'dsfs');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` varchar(10) NOT NULL,
  `password` varchar(20) NOT NULL,
  `user_name` varchar(40) NOT NULL,
  `user_type` varchar(5) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `password`, `user_name`, `user_type`) VALUES
('del123', 'password', 'delete user', 'staff'),
('du023', 'password', 'Dummy User 1', 'staff'),
('du123', 'password', 'Dummy User 2', 'admin'),
('du321', 'password', 'dummy user 3', 'staff'),
('Fe023', 'adminpass', 'Festus', 'admin'),
('Ma023', 'password', 'Mando', 'staff'),
('Wi023', 'adminpass', 'William', 'admin');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `company_data`
--
ALTER TABLE `company_data`
  ADD PRIMARY KEY (`companyIdentifier`);

--
-- Indexes for table `inventory_data`
--
ALTER TABLE `inventory_data`
  ADD PRIMARY KEY (`product_id`);

--
-- Indexes for table `pos_system`
--
ALTER TABLE `pos_system`
  ADD PRIMARY KEY (`pos_id`);

--
-- Indexes for table `tool_data`
--
ALTER TABLE `tool_data`
  ADD PRIMARY KEY (`tool_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `inventory_data`
--
ALTER TABLE `inventory_data`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=115;

--
-- AUTO_INCREMENT for table `tool_data`
--
ALTER TABLE `tool_data`
  MODIFY `tool_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
