variable "aws_region" {
  description = "AWS deployment region"
  type        = string
  default     = "ap-south-1"
}

variable "project_name" {
  description = "Prefix for FBS resource names"
  type        = string
  default     = "fbs"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

variable "key_name" {
  description = "Existing AWS EC2 key pair name"
  type        = string
  default     = "ec2-keypair"
}

variable "ssh_cidr" {
  description = "Public IP allowed to connect over SSH"
  type        = string
  default     = "103.210.133.107/32"
}
