import boto3
import os

eks = boto3.client('eks')

def handler(event, context):
    action = event.get('action') # 'down' (종료) 또는 'up' (기동)
    cluster_name = os.environ['CLUSTER_NAME']
    
    # EKS 클러스터 내부의 노드 그룹 목록을 조회하여 'initial'로 시작하는 노드 그룹 탐색
    nodegroups_resp = eks.list_nodegroups(clusterName=cluster_name)
    nodegroups = nodegroups_resp.get('nodegroups', [])
    nodegroup_name = None
    for ng in nodegroups:
        if ng.startswith('initial'):
            nodegroup_name = ng
            break
            
    if not nodegroup_name:
        if nodegroups:
            nodegroup_name = nodegroups[0]
        else:
            raise Exception(f"No nodegroups found in cluster {cluster_name}")
            
    if action == 'down':
        min_size = 0
        max_size = 1 # EKS 제약: max_size는 1 이상이어야 함
        desired_size = 0
    else:
        min_size = 1
        max_size = 3
        desired_size = 2
        
    print(f"Updating nodegroup {nodegroup_name} config: min={min_size}, max={max_size}, desired={desired_size}")
    
    response = eks.update_nodegroup_config(
        clusterName=cluster_name,
        nodegroupName=nodegroup_name,
        scalingConfig={
            'minSize': min_size,
            'maxSize': max_size,
            'desiredSize': desired_size
        }
    )
    return {
        'statusCode': 200,
        'body': f"EKS nodegroup {nodegroup_name} config update initiated successfully."
    }
