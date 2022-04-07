create table enduser (
	id bigserial primary key,
	auth_id varchar(128) not null unique,
	username varchar(128),
	is_valid boolean default false,
	slug varchar(128) unique,
	email varchar(320),
	profile_image_url varchar(128),
	bio varchar(2048),
	created_at timestamp default current_timestamp
);
create table enduser_follow (
	enduser_id bigint,
	created_by bigint,
	created_at timestamp default current_timestamp,
	foreign key(enduser_id) references enduser(id),
	foreign key(created_by) references enduser(id),
	primary key (enduser_id, created_by)
);
create table enduser_chat (
	id bigserial primary key,
	enduser_id bigint not null,
	created_by bigint not null,
	created_at timestamp default current_timestamp,
	unique (enduser_id, created_by, created_at),
	foreign key(enduser_id) references enduser(id),
	foreign key(created_by) references enduser(id)
);
create table enduser_message (
	chat_id bigint not null,
	enduser_id bigint not null,
	created_by bigint not null,
	created_at timestamp default current_timestamp,
	message varchar(512) not null,
	primary key (chat_id, enduser_id, created_by, created_at),
	foreign key(chat_id) references enduser_chat(id),
	foreign key(created_by) references enduser(id)
);
--;;
create table post_image (
	id bigserial primary key,
	name varchar(128) not null,
	url varchar(2048),
	created_by bigint,
	created_at timestamp default current_timestamp,
	foreign key(created_by) references enduser(id)
);
create table post_type (
	id bigserial primary key,
	name varchar(128) not null,
	created_at timestamp default current_timestamp
);
create table post (
	id bigserial primary key,
	post_type_id bigint,
	title varchar(128) not null,
	post_image_id bigint,
	content text,
	link varchar(2048),
	like_count integer default 0 not null,
	repost_count integer default 0 not null,
	comment_count integer default 0 not null,
	created_by bigint,
	created_at timestamp default current_timestamp,
	foreign key(created_by) references enduser(id),
	foreign key(post_type_id) references post_type(id),
	foreign key(post_image_id) references post_image(id)
);
create table tag (
	id bigserial primary key,
	name varchar(128) not null,
	created_at timestamp default current_timestamp
);
create table post_tag (
	post_id bigint,
	tag_id bigint,
	foreign key(post_id) references post(id),
	foreign key(tag_id) references tag(id),
	primary key (post_id, tag_id)
);
create table post_like (
	post_id bigint,
	created_by bigint,
	created_at timestamp default current_timestamp,
	primary key (post_id, created_by),
	--;; unique (post_id, created_by),
	foreign key(created_by) references enduser(id),
	foreign key(post_id) references post(id)
);
create table post_dislike (
	post_id bigint,
	created_by bigint,
	created_at timestamp default current_timestamp,
	primary key (post_id, created_by),
	foreign key(created_by) references enduser(id),
	foreign key(post_id) references post(id)
);
--;;
create table tag_follow (
	tag_id bigint,
	created_by bigint,
	created_at timestamp default current_timestamp,
	foreign key (tag_id) references tag(id),
	foreign key (created_by) references enduser(id),
	primary key (tag_id, created_by)
);
create table post_comment (
	id bigserial primary key,
	post_id bigint,
	comment varchar(1024),
	like_count integer default 0 not null,
	created_by bigint,
	created_at timestamp default current_timestamp,
	foreign key(created_by) references enduser(id),
	foreign key(post_id) references post(id)
);
create table post_comment_like (
	post_comment_id bigint,
	created_by bigint,
	created_at timestamp default current_timestamp,
	primary key (post_comment_id, created_by),
	foreign key(created_by) references enduser(id),
	foreign key(post_comment_id) references post_comment(id)
);
create table post_comment_dislike (
	post_comment_id bigint,
	created_by bigint,
	created_at timestamp default current_timestamp,
	primary key (post_comment_id, created_by),
	foreign key(created_by) references enduser(id),
	foreign key(post_comment_id) references post_comment(id)
);
create table post_comment_reply (
	id bigserial primary key,
	comment_id bigint,
	reply varchar(1024),
	like_count integer default 0 not null,
	created_by bigint,
	created_at timestamp default current_timestamp,
	foreign key(created_by) references enduser(id),
	foreign key(comment_id) references post_comment(id)
);
create table post_comment_reply_like (
	post_comment_reply_id bigint,
	created_by bigint,
	created_at timestamp default current_timestamp,
	primary key (post_comment_reply_id, created_by),
	foreign key(created_by) references enduser(id),
	foreign key(post_comment_reply_id) references post_comment_reply(id)
);
create table post_comment_reply_dislike (
	post_comment_reply_id bigint,
	created_by bigint,
	created_at timestamp default current_timestamp,
	primary key (post_comment_reply_id, created_by),
	foreign key(created_by) references enduser(id),
	foreign key(post_comment_reply_id) references post_comment_reply(id)
);
