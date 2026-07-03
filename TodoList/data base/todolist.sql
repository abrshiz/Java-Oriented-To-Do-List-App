-- phpMyAdmin SQL Dump
-- version 5.2.3deb1
-- https://www.phpmyadmin.ne
-- Host: localhost:3306
-- Generation Time: Dec 19, 2025 at 12:55 PM
-- Server version: 11.8.5-MariaDB-1 from Debian
-- PHP Version: 8.4.11

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `todolist`
--
CREATE DATABASE IF NOT EXISTS `todolist` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci;
USE `todolist`;

-- --------------------------------------------------------

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks` (
  `id` int(11) NOT NULL,
  `task_text` varchar(500) NOT NULL,
  `category` varchar(50) NOT NULL,
  `deadline` datetime NOT NULL,
  `completed` tinyint(1) DEFAULT 0,
  `notified` tinyint(1) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `snoozed_until` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `tasks`
--

INSERT DELAYED IGNORE INTO `tasks` (`id`, `task_text`, `category`, `deadline`, `completed`, `notified`, `created_at`, `snoozed_until`) VALUES
(71, 'b', 'Work', '2025-12-19 08:03:00', 1, 0, '2025-12-19 05:02:43', NULL),
(72, 'kk', 'Work', '2025-12-19 08:04:00', 1, 0, '2025-12-19 05:03:46', NULL),
(73, 'ff', 'Work', '2025-12-19 08:06:00', 1, 1, '2025-12-19 05:04:45', NULL),
(74, 'm', 'Work', '2025-12-19 08:08:00', 1, 1, '2025-12-19 05:06:27', NULL),
(75, 't', 'Personal', '2025-12-19 08:09:00', 1, 0, '2025-12-19 05:07:33', NULL),
(78, 'opp', 'Other', '2025-12-19 08:39:00', 1, 0, '2025-12-19 05:37:46', NULL),
(79, 'bas', 'Work', '2025-12-19 14:58:00', 1, 0, '2025-12-19 11:56:50', NULL),
(81, 'bbb', 'Work', '2025-12-19 15:36:00', 0, 1, '2025-12-19 12:35:14', '2025-12-19 15:45:32');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `tasks`
--
ALTER TABLE `tasks`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_deadline` (`deadline`),
  ADD KEY `idx_completed` (`completed`),
  ADD KEY `idx_category` (`category`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `tasks`
--
ALTER TABLE `tasks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=82;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
