# --- Build -------------------------------------------------------------
FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci

COPY . .

# VITE_API_URL pode ser passado como build arg pelo Railway.
ARG VITE_API_URL
ARG VITE_USE_MOCKS=false

ENV VITE_API_URL=$VITE_API_URL
ENV VITE_USE_MOCKS=$VITE_USE_MOCKS

RUN npm run build

# --- Runtime -----------------------------------------------------------
FROM nginx:1.27-alpine

COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

ENV PORT=8080

EXPOSE 8080
