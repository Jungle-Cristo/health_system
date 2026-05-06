/**
 * 健康数据录入脚本
 * 生成30天的健康数据（包含正常期和2段患病期），通过REST API录入
 *
 * 用法：确保后端已启动（mvn spring-boot:run），然后运行 node seed-data.js
 * 注意：后端使用H2内存数据库，重启后数据会丢失，需重新运行本脚本
 */

const BASE_URL = 'http://localhost:8080/api';
const TEST_USER = { username: 'testuser', password: 'Test123456', email: 'test@health.com' };
const DAYS = 30;

// ---- 健康数据类型定义 ----
const HEALTH_TYPES = [
  { type: 'steps',           unit: '步',     normal: [5000, 12000], decimals: 0 },
  { type: 'heart_rate',      unit: 'bpm',    normal: [62, 85],      decimals: 0 },
  { type: 'sleep',           unit: '小时',   normal: [6.5, 8.5],    decimals: 1 },
  { type: 'weight',          unit: 'kg',     normal: [68.5, 71.0],  decimals: 1 },
  { type: 'blood_pressure',  unit: 'mmHg',   normal: [108, 128],    decimals: 0 },
  { type: 'blood_sugar',     unit: 'mmol/L', normal: [4.2, 5.8],    decimals: 1 },
];

// 两个异常窗口期
// 第10-12天 (day 10, 11, 12)：发烧感冒 — 步数骤降、心率升高、睡眠差、血压偏高
// 第22-24天 (day 22, 23, 24)：血糖异常 — 血糖飙升，其他正常
const SICK_WINDOWS = {
  fever:    { start: 10, end: 12, types: {
    steps:           [800, 3000],
    heart_rate:      [88, 110],
    sleep:           [4.0, 6.0],
    weight:          [68.0, 70.0],
    blood_pressure:  [130, 155],
    blood_sugar:     [4.2, 5.8],  // 正常
  }},
  bloodSugar: { start: 22, end: 24, types: {
    // 除血糖外其余正常
    blood_sugar:     [7.2, 9.5],
  }},
};

// ---- 工具函数 ----
function fmtDate(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function rand(min, max, decimals = 1) {
  const val = Math.random() * (max - min) + min;
  return decimals === 0 ? Math.round(val) : +val.toFixed(decimals);
}

function getSickRange(dayIndex, typeKey) {
  if (dayIndex >= SICK_WINDOWS.fever.start && dayIndex <= SICK_WINDOWS.fever.end) {
    const r = SICK_WINDOWS.fever.types[typeKey];
    if (r) return r;
  }
  if (dayIndex >= SICK_WINDOWS.bloodSugar.start && dayIndex <= SICK_WINDOWS.bloodSugar.end) {
    const r = SICK_WINDOWS.bloodSugar.types[typeKey];
    if (r) return r;
  }
  return null;
}

async function apiPost(path, body, token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers,
    body: JSON.stringify(body),
  });

  const data = await res.json();
  if (!res.ok && res.status !== 400) {  // 400 could be "user exists"
    throw new Error(`${path} ${res.status}: ${JSON.stringify(data)}`);
  }
  return data;
}

function sleep(ms) {
  return new Promise(r => setTimeout(r, ms));
}

// ---- 主流程 ----
async function main() {
  console.log('=== 健康数据录入脚本 ===\n');

  // 1. 注册/登录获取 token
  let token;
  try {
    console.log('正在注册测试用户...');
    const reg = await apiPost('/auth/register', TEST_USER);
    if (reg.code === 200 && reg.data) {
      token = reg.data.token;
      console.log(`  注册成功，用户ID: ${reg.data.userId}, 用户名: ${reg.data.username}`);
    } else if (reg.code === 409 || (reg.message && reg.message.includes('已存在'))) {
      console.log('  用户已存在，尝试登录...');
      const login = await apiPost('/auth/login', {
        account: TEST_USER.username,
        password: TEST_USER.password,
      });
      if (login.code === 200 && login.data) {
        token = login.data.token;
        console.log(`  登录成功，用户ID: ${login.data.userId}`);
      }
    } else {
      console.error('注册失败:', JSON.stringify(reg));
      process.exit(1);
    }
  } catch (e) {
    console.error('认证失败:', e.message);
    console.error('请确保后端已启动: cd health-management-system && mvn spring-boot:run');
    process.exit(1);
  }

  // 2. 生成30天数据
  const today = new Date();
  let totalCount = 0;

  console.log(`\n开始录入最近 ${DAYS} 天健康数据...\n`);

  for (let d = DAYS - 1; d >= 0; d--) {
    const date = new Date(today);
    date.setDate(date.getDate() - d);
    const dayIndex = DAYS - 1 - d;
    const dateStr = fmtDate(date);
    const dayLabel = `${dateStr} (第${dayIndex + 1}天)`;

    // 判断当天是否在异常窗口
    let dayStatus = '正常';
    if (dayIndex >= SICK_WINDOWS.fever.start && dayIndex <= SICK_WINDOWS.fever.end) {
      dayStatus = '发烧感冒期';
    } else if (dayIndex >= SICK_WINDOWS.bloodSugar.start && dayIndex <= SICK_WINDOWS.bloodSugar.end) {
      dayStatus = '血糖异常期';
    }

    const dataPoints = [];

    for (const ht of HEALTH_TYPES) {
      const sickRange = getSickRange(dayIndex, ht.type);
      const [lo, hi] = sickRange || ht.normal;
      const value = rand(lo, hi, ht.decimals);

      dataPoints.push({
        type: ht.type,
        value: value,
        unit: ht.unit,
        recordDate: dateStr,
      });
    }

    // 逐条发送
    for (const dp of dataPoints) {
      try {
        await apiPost('/health/data', dp, token);
        totalCount++;
      } catch (e) {
        console.error(`  录入失败 [${dp.type}]:`, e.message);
      }
      await sleep(30); // 避免过快请求
    }

    // 打印当天录入摘要
    const stepInfo = dataPoints.find(p => p.type === 'steps');
    const hrInfo = dataPoints.find(p => p.type === 'heart_rate');
    const bsInfo = dataPoints.find(p => p.type === 'blood_sugar');
    console.log(`  ${dayLabel} [${dayStatus}] 步数:${stepInfo.value} 心率:${hrInfo.value} 血糖:${bsInfo.value}`);
  }

  console.log(`\n=== 录入完成！共录入 ${totalCount} 条健康数据 ===`);
  console.log('涵盖6种类型 x 30天 = 180条数据');
  console.log('包含异常窗口期：第10-12天（发烧感冒）、第22-24天（血糖异常）\n');
}

main().catch(e => {
  console.error('脚本执行失败:', e);
  process.exit(1);
});
