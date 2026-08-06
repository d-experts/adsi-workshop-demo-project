import path from "node:path";
import type { NextConfig } from "next";

const isDev = process.env.NODE_ENV === "development";

const nextConfig: NextConfig = {
  // dev: /api をバックエンド(localhost:8080)へ転送する。
  // 本番デプロイ: static export（S3 + CloudFront 配信）で、API は別途配信する。
  ...(isDev
    ? {
        async rewrites() {
          return [
            {
              source: "/api/:path*",
              destination: "http://localhost:8080/api/:path*",
            },
          ];
        },
      }
    : { output: "export" }),
  turbopack: {
    root: path.resolve(__dirname),
  },
};

export default nextConfig;
