FROM node:18-alpine AS build
WORKDIR /app
COPY package.json ./
COPY package-lock.json ./
RUN npm install
COPY . ./
ENV NODE_ENV=production
RUN REACT_APP_API_URL=http://backend:8080/api npm run build

FROM nginx:stable-alpine
# 커스텀 Nginx 설정 파일을 메인 설정 파일로 복사
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]