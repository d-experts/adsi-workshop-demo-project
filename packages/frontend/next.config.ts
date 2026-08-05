import path from "node:path";
import type { NextConfig } from "next";

const isDev = process.env.NODE_ENV === "development";

const nextConfig: NextConfig = {
  // 本番デプロイは static export（S3 + CloudFront で配信）。
  // dev では export せず、/api をバックエンドへ転送する。
  ...(isDev ? {} : { output: "export" }),
  turbopack: {
    root: path.resolve(__dirname),
  },
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
    : {}),
};

export default nextConfig;
