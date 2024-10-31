# 단계 1: 빌드 단계
FROM node:18-alpine AS build

WORKDIR /app

# p7zip 패키지 설치 및 심볼릭 링크 생성
RUN apk add --no-cache p7zip \
    && ln -s /usr/bin/7z /usr/bin/7za  # electron-builder가 7za 경로를 참조하도록 설정

# 의존성 설치
COPY package.json package-lock.json ./
ENV USE_SYSTEM_7ZIP true
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
