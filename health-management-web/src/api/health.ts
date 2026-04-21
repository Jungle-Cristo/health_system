import request from '../utils/request';

export interface HealthDataRequest {
  type: string;
  value: number;
  unit: string;
  recordDate?: string;
  notes?: string;
}

export interface HealthDataResponse {
  id: number;
  type: string;
  value: number;
  unit: string;
  recordDate: string;
  notes?: string;
  createdAt: string;
}

export interface HealthDataTrend {
  date: string;
  value: number;
}

// 添加健康数据
export function addHealthData(data: HealthDataRequest) {
  return request.post<any, { data: HealthDataResponse }>('/health/data', data);
}

// 获取健康数据列表
export function getHealthDataList(
  type: string,
  startDate?: string,
  endDate?: string
) {
  return request.get<any, { data: HealthDataResponse[] }>('/health/data', {
    params: {
      type,
      startDate,
      endDate
    }
  });
}

// 获取健康数据趋势
export function getHealthDataTrend(type: string, period: string) {
  return request.get<any, { data: HealthDataResponse[] }>('/health/trend', {
    params: {
      type,
      period
    }
  });
}

// 删除健康数据
export function deleteHealthData(id: number) {
  return request.delete<any, { data: null }>(`/health/data/${id}`);
}

// 获取所有健康数据类型
export const healthDataTypes = [
  { label: '步数', value: 'steps', unit: '步' },
  { label: '心率', value: 'heart_rate', unit: 'bpm' },
  { label: '睡眠', value: 'sleep', unit: '小时' },
  { label: '体重', value: 'weight', unit: 'kg' },
  { label: '血压', value: 'blood_pressure', unit: 'mmHg' },
  { label: '血糖', value: 'blood_sugar', unit: 'mmol/L' }
];

// 获取单位
export function getUnitByType(type: string): string {
  const item = healthDataTypes.find(t => t.value === type);
  return item?.unit || '';
}

// 获取标签
export function getLabelByType(type: string): string {
  const item = healthDataTypes.find(t => t.value === type);
  return item?.label || type;
}
