# 1. 사용할 클라우드 제공자(AWS) 설정
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# 2. AWS 리전 설정 (서울)
provider "aws" {
  region = "ap-northeast-2"
}

# 3. VPC 및 서브넷 네트워크 뼈대 생성 (공식 모듈 사용)
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"

  name = "smart-safety-vpc"
  cidr = "10.0.0.0/16"

  # NAT 게이트웨이가 있는 2d 가용 영역을 무조건 첫 번째로 배치!
  azs             = ["ap-northeast-2d", "ap-northeast-2a"]
  
  # 2d의 프라이빗 대역(4.0)을 첫 번째로, 2a의 대역(3.0)을 두 번째로 변경
  private_subnets = ["10.0.4.0/24", "10.0.3.0/24"]
  
  # 2d의 퍼블릭 대역(1.0)을 첫 번째로, 2a의 대역(2.0)을 두 번째로 변경
  public_subnets  = ["10.0.1.0/24", "10.0.2.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true 

  enable_dns_hostnames = true
  enable_dns_support   = true

 #쿠버네티스 로드밸런서 자동 생성을 위한 태그
  public_subnet_tags = {
    "kubernetes.io/role/elb" = "1"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = "1"
  }
}

# ==========================================
# 1. 오직 프라이빗 서브넷만 포함된 DB 서브넷 그룹 생성
# ==========================================
resource "aws_db_subnet_group" "private" {
  name       = "smart-safety-db-private-subnet-group"
  
  # VPC 모듈에서 만든 '프라이빗 서브넷(10.0.3.0, 10.0.4.0)'만 콕 집어서 배정합니다!
  subnet_ids = module.vpc.private_subnets

  tags = {
    Name = "smart-safety-db-private-subnet-group"
  }
}

# ==========================================
# 2. 스냅샷(세이브 파일)을 이용한 새 DB 인스턴스 복원
# ==========================================
resource "aws_db_instance" "restored_db" {
  identifier          = "smart-safety-db-new" # 기존 DB와 이름이 겹치지 않게 '-new'를 붙입니다.
  
  # 💡 방금 찍어둔 스냅샷 이름을 여기에 적어 세이브 파일을 불러옵니다!
  snapshot_identifier = "smart-safety-db-snapshot-01-0707" 
  
  instance_class      = "db.t3.micro" # 기존 사양에 맞춤 (필요시 db.t3.small 등으로 변경)

  # 방금 위에서 만든 '프라이빗 전용 서브넷 그룹'을 연결 (핵심!)
  db_subnet_group_name   = aws_db_subnet_group.private.name
  
  # 아까 확인했던 기존 완벽한 RDS 보안 그룹(smart-safety-rds-sg)의 ID를 그대로 연결
  vpc_security_group_ids = ["sg-084077e3657dc1307"] 

  # 공인 IP 원천 차단! (절대 인터넷에서 접근 불가)
  publicly_accessible = false 

  # 테스트/개발용이므로 추후 삭제하기 편하게 설정
  skip_final_snapshot = true

  storage_encrypted   = true 
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  cluster_name    = "smart-safety-eks-new"
  cluster_version = "1.35"

  cluster_endpoint_public_access = true

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

eks_managed_node_groups = {
    initial = {
      min_size     = 1  
      max_size     = 3  
      desired_size = 2  
      instance_types = ["t3.medium"]
      capacity_type  = "ON_DEMAND"
    }
  }

  tags = {
    Environment = "dev"
    Terraform   = "true"
  }
}