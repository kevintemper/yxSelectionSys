// yx-admin/src/components/SvgCaptcha.tsx
import React, { useState, useCallback, useEffect } from 'react';
import { Button, Space } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';

interface SvgCaptchaProps {
    onCaptchaChange: (code: string) => void;
    width?: number;
    height?: number;
}

const SvgCaptcha: React.FC<SvgCaptchaProps> = ({
    onCaptchaChange,
    width = 120,
    height = 40
}) => {
    const [captchaCode, setCaptchaCode] = useState('');
    const [svgContent, setSvgContent] = useState('');

    // 生成随机验证码文本（4位数字字母）
    const generateRandomCode = useCallback(() => {
        const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
        let code = '';
        for (let i = 0; i < 4; i++) {
            code += chars[Math.floor(Math.random() * chars.length)];
        }
        return code;
    }, []);

    // 生成SVG验证码
    const generateSvgCaptcha = useCallback(() => {
        const code = generateRandomCode();
        setCaptchaCode(code);
        onCaptchaChange(code);

        // 创建SVG内容（带干扰线和干扰点）
        const svg = `
      <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
        <rect width="100%" height="100%" fill="#f8f9fa"/>
        ${code.split('').map((char, index) => {
            const x = (index * width / 4) + 10;
            const y = height / 2 + Math.random() * 10 - 5;
            const rotation = Math.random() * 30 - 15;
            const fontSize = 20 + Math.random() * 6;
            return `
            <text 
              x="${x}" 
              y="${y}" 
              transform="rotate(${rotation} ${x} ${y})"
              font-family="Arial, sans-serif" 
              font-size="${fontSize}" 
              font-weight="bold"
              fill="${index % 2 === 0 ? '#1890ff' : '#ff4d4f'}"
              text-anchor="middle"
              dominant-baseline="central"
            >${char}</text>
          `;
        }).join('')}
        <!-- 干扰线 -->
        ${Array.from({ length: 3 }, (_, i) => {
            const x1 = Math.random() * width;
            const y1 = Math.random() * height;
            const x2 = Math.random() * width;
            const y2 = Math.random() * height;
            return `
            <line 
              x1="${x1}" 
              y1="${y1}" 
              x2="${x2}" 
              y2="${y2}" 
              stroke="#ddd" 
              stroke-width="1"
            />
          `;
        }).join('')}
        <!-- 干扰点 -->
        ${Array.from({ length: 30 }, (_, i) => {
            const cx = Math.random() * width;
            const cy = Math.random() * height;
            const r = Math.random() * 2 + 0.5;
            return `
            <circle 
              cx="${cx}" 
              cy="${cy}" 
              r="${r}" 
              fill="${Math.random() > 0.5 ? '#999' : '#ccc'}"
            />
          `;
        }).join('')}
      </svg>
    `;

        setSvgContent(svg);
    }, [generateRandomCode, onCaptchaChange, width, height]);

    // 初始化生成验证码
    useEffect(() => {
        generateSvgCaptcha();
    }, [generateSvgCaptcha]);

    // 处理刷新验证码
    const handleRefresh = () => {
        generateSvgCaptcha();
    };

    return (
        <Space.Compact style={{ display: 'flex', alignItems: 'center' }}>
            <div
                dangerouslySetInnerHTML={{ __html: svgContent }}
                onClick={handleRefresh}
                style={{
                    cursor: 'pointer',
                    border: '1px solid #d9d9d9',
                    borderRadius: '6px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: '#f8f9fa',
                    minWidth: width,
                    height: height
                }}
                title="点击刷新验证码"
            />
            <Button
                type="text"
                icon={<ReloadOutlined />}
                onClick={handleRefresh}
                style={{ height: height }}
                title="刷新验证码"
            />
        </Space.Compact>
    );
};

export default SvgCaptcha;