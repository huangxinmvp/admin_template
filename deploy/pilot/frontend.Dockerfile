ARG FRONTEND_NODE_BASE_IMAGE=docker.m.daocloud.io/library/node:20-bookworm-slim
ARG FRONTEND_NGINX_BASE_IMAGE=docker.m.daocloud.io/library/nginx:1.27-alpine
FROM ${FRONTEND_NODE_BASE_IMAGE} AS build

WORKDIR /workspace/web_frontend

COPY web_frontend/package.json web_frontend/package-lock.json ./

ARG REACT_APP_API_BASE_URL=

ENV CI=true
ENV HUSKY=0
ENV REACT_APP_ENV=pre
ENV REACT_APP_API_BASE_URL=${REACT_APP_API_BASE_URL}

RUN npm ci --legacy-peer-deps

COPY web_frontend/ ./

RUN npm run build

FROM ${FRONTEND_NGINX_BASE_IMAGE}

COPY deploy/pilot/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/web_frontend/dist /usr/share/nginx/html

EXPOSE 80
