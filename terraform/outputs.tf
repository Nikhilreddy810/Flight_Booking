output "vpc_id" {
  value = aws_vpc.fbs.id
}

output "public_subnet_id" {
  value = aws_subnet.public.id
}

output "security_group_id" {
  value = aws_security_group.app.id
}

output "instance_id" {
  value = aws_instance.fbs.id
}

output "public_ip" {
  value = aws_instance.fbs.public_ip
}

output "http_url" {
  value = "http://${aws_instance.fbs.public_ip}"
}
