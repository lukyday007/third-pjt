# 단계 1: 빌드 단계
FROM node:16-alpine AS build

WORKDIR /app

# 의존성 설치
COPY package.json package-lock.json ./
RUN npm install

# .env 파일 복사(필요하다면)
# COPY .env .env

# 소스 코드 복사 및 빌드
COPY . .
RUN npm run build

# 단계 2: 실행 단계
FROM nginx:alpine

# 빌드 결과물을 Nginx의 기본 경로로 복사
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx 템플릿 파일을 복사 (nginx.template 사용)
COPY nginx.template /etc/nginx/conf.d/default.conf.template

# Nginx 설정 파일에 환경 변수를 치환하고 적용
CMD ["/bin/sh", "-c", "envsubst '${LISTEN_PORT}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
