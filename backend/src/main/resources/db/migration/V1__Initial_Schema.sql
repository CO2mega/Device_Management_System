-- V1__Initial_Schema.sql
-- Device Management System - Initial Database Schema

-- Users table
CREATE TABLE t_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id VARCHAR(50) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on staff_id for faster lookups
CREATE INDEX idx_user_staff_id ON t_user(staff_id);

-- Devices table
CREATE TABLE t_device (
    device_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50),
    location VARCHAR(200),
    purchase_date DATE,
    is_loaned BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) DEFAULT '正常',
    current_holder_id BIGINT,
    loan_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (current_holder_id) REFERENCES t_user(user_id) ON DELETE SET NULL
);

-- Create indexes for common queries
CREATE INDEX idx_device_name ON t_device(device_name);
CREATE INDEX idx_device_is_loaned ON t_device(is_loaned);
CREATE INDEX idx_device_status ON t_device(status);

-- Loan records table
CREATE TABLE t_loan_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    applicant_user_id BIGINT NOT NULL,
    apply_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_return_date DATE NOT NULL,
    actual_return_date TIMESTAMP,
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    return_status VARCHAR(20),
    purpose VARCHAR(500),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (device_id) REFERENCES t_device(device_id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_user_id) REFERENCES t_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES t_user(user_id) ON DELETE SET NULL
);

-- Create indexes for common queries
CREATE INDEX idx_loan_device_id ON t_loan_record(device_id);
CREATE INDEX idx_loan_applicant_id ON t_loan_record(applicant_user_id);
CREATE INDEX idx_loan_approval_status ON t_loan_record(approval_status);
CREATE INDEX idx_loan_apply_date ON t_loan_record(apply_date);
