import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: '/demo-data',
        destination: 'http://127.0.0.1:8080/demo-data',
      },
      {
        source: '/demo-data/:path*',
        destination: 'http://127.0.0.1:8080/demo-data/:path*',
      },
      {
        source: '/timetables/:path*', 
        destination: 'http://127.0.0.1:8080/timetables/:path*',
      },
    ]
  },
}

export default nextConfig